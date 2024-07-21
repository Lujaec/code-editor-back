package com.example.webcompiler.docker.service;

import com.example.webcompiler.docker.entity.ContainerStatus;
import com.example.webcompiler.docker.entity.MyContainer;
import com.example.webcompiler.docker.repository.ContainerRepository;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.Ports;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DockerServiceImpl implements DockerService{
    private  final DockerClient dockerClient;

    private final ContainerRepository containerRepository;
    private final int DEFAULT_SIZE = 3;
    private final int CONTAINER_INACTIVITY_THRESHOLD_MINUTES = 30;

    @PostConstruct
    public void postConstruct() throws IOException {
        log.info("Docker 컨테이너 리스트 조회");
        initInactiveContainerRepository();
    }

    @Scheduled(fixedRate = 60000) // 60초마다 실행
    public void removeInactiveContainers() {
        log.debug("비활성 컨테이너 삭제 작업 시작");
        Deque<MyContainer> inactiveContainers = containerRepository.getInActiveContainers();
        LocalDateTime now = LocalDateTime.now();

        for (MyContainer container : inactiveContainers) {
            log.debug("containerId = {}, lastUsed = {}, now = {}", container.getContainerId(), container.getLastUsed(), LocalDateTime.now());

            if (container.getLastUsed() != null && now.minusMinutes(CONTAINER_INACTIVITY_THRESHOLD_MINUTES).isAfter(container.getLastUsed())) {
                deleteContainer(container);
            }
        }

        log.debug("비활성 컨테이너 삭제 작업 종료");
    }

    @Override
    public MyContainer createContainer(String userUUID) throws IOException {
        UUID uuid = UUID.randomUUID();

        ExposedPort exposedPort = ExposedPort.tcp(22);
        Ports portBindings = new Ports();
        portBindings.bind(exposedPort, Ports.Binding.bindPort(0));

        String containerName = "ubuntu-compiler-" + uuid;
        CreateContainerResponse container = dockerClient.createContainerCmd("ubuntu-compiler")
                .withEnv(List.of(uuid.toString()))
                .withName(containerName)
                .withExposedPorts(exposedPort)
                .withPortBindings(portBindings)
                .withTty(true)
                .withAttachStdin(true)
                .withAttachStdout(true)
                .withAttachStderr(true)
                .exec();


        log.info("DockerServiceImpl create container {}", containerName);
        MyContainer createdMyContainer = new MyContainer(container.getId(), containerName, LocalDateTime.now());
        containerRepository.saveInActiveContainer(createdMyContainer);
        return createdMyContainer;
    }

    @Override
    public void runContainer(MyContainer myContainer) throws InterruptedException {
        dockerClient.startContainerCmd(myContainer.getContainerId())
                .exec();

        List<Container> containers = dockerClient.listContainersCmd().exec();

        for (Container container : containers) {
            if(container.getId().equals(myContainer.getContainerId())){
                myContainer.setPublicPort(container.getPorts()[0].getPublicPort());
                myContainer.setLastUsed(LocalDateTime.now());
            }
        }
    }

    @Override
    public void prepareContainers() throws IOException {

        ExposedPort exposedPort = ExposedPort.tcp(22);
        Ports portBindings = new Ports();
        portBindings.bind(exposedPort, Ports.Binding.bindPort(0));

        for(int i = 0; i < DEFAULT_SIZE; ++i){
            UUID uuid = UUID.randomUUID();
            String containerName = "ubuntu-compiler-" + uuid;
            CreateContainerResponse container = dockerClient.createContainerCmd("ubuntu-compiler:v1.0")
                    .withEnv(List.of(uuid.toString()))
                    .withName(containerName)
                    .withExposedPorts(exposedPort)
                    .withPortBindings(portBindings)
                    .withTty(true)
                    .withAttachStdin(true)
                    .withAttachStdout(true)
                    .withAttachStderr(true)
                    .exec();

            log.info("DockerServiceImpl create container {}", containerName);
            MyContainer createdMyContainer = new MyContainer(container.getId(), containerName, LocalDateTime.now());
            containerRepository.saveInActiveContainer(createdMyContainer);
        }
    }

    /*
    * 사용자가 실행중인 컨테이너를 소유할 경우 실행 중인 컨테이너를 반환
    * 사용자가 실행중인 컨테이너를 소유하지 않은 경우 실행 중인 컨테이너를 할당
     */
    @Override
    public MyContainer allocateContainer(String userUUID) throws IOException, InterruptedException {
        MyContainer activeContainer = containerRepository.getActiveContainer(userUUID);

        if(activeContainer != null) {
            log.info("해당 사용자는 이미 Container가 할당된 상태입니다. userUUID = {}, contrainerID = {}", userUUID, activeContainer.getContainerId());
            return activeContainer;
        }

        MyContainer allocContainer = containerRepository.popInActiveContainer();

        if (allocContainer == null){
            log.info("exited상태인 컨테이너가 존재하지 않습니디. 컨태이너 추가");
            prepareContainers();
            allocContainer = containerRepository.popInActiveContainer();
        }

        runContainer(allocContainer);
        return containerRepository.saveActiveContainer(userUUID, allocContainer);
    }

    @Override
    public void stopContainer(String userUUID) throws IOException {
        MyContainer stopContainer = containerRepository.popActiveContainer(userUUID);

        if(stopContainer == null){
            log.info("[DockerService#stopContainer] 활성화된 컨테이너를 찾을 수 없습니다. userUUID = {}", userUUID);
            return;
        }

        containerRepository.saveInActiveContainer(stopContainer);
        dockerClient.stopContainerCmd(stopContainer.getContainerId())
                .exec();
    }

    @Override
    public void deleteContainer(MyContainer myContainer) {
        log.info("[DockerService#deleteContainer] 컨테이너 삭제: ID = {}, 이름 = {}", myContainer.getContainerId(), myContainer.getContainerName());
        dockerClient.removeContainerCmd(myContainer.getContainerId()).exec();
        containerRepository.removeInActiveContainer(myContainer.getContainerId());
    }

    private void initInactiveContainerRepository() throws IOException {
        List<Container> containers = new ArrayList<>();

        containers.addAll(dockerClient.listContainersCmd()
                .withShowAll(true)
                .withStatusFilter("created")
                .exec());

        containers.addAll(dockerClient.listContainersCmd()
                .withShowAll(true)
                .withStatusFilter("exited")
                .exec());

        for (Container container : containers) {
            if (container.getNames()[0].contains("ubuntu-compiler")) {
                ContainerStatus containerStatus = ContainerStatus.fromString(container.getStatus().split(" ")[0]);

                if (containerStatus.equals(ContainerStatus.CREATED) || containerStatus.equals(ContainerStatus.EXITED)) {
                    MyContainer myContainer = new MyContainer(container.getId(), container.getNames()[0], LocalDateTime.now());

                    containerRepository.saveInActiveContainer(myContainer);
                }
            }
        }
    }
}
