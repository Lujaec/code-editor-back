package com.example.webcompiler.docker.service;

import com.example.webcompiler.docker.entity.MyContainer;
import com.example.webcompiler.docker.repository.ContainerRepository;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.Ports;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DockerServiceImpl implements DockerService{
    private  final DockerClient dockerClient;

    private final ContainerRepository containerRepository;
    private ExecutorService executorService = Executors.newCachedThreadPool();

    private final int DEFAULT_SIZE = 3;

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
        MyContainer createdMyContainer = new MyContainer(container.getId(), containerName);
        containerRepository.saveExitedContainer(createdMyContainer);
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
            MyContainer createdMyContainer = new MyContainer(container.getId(), containerName);
            containerRepository.saveExitedContainer(createdMyContainer);
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

        MyContainer allocContainer = containerRepository.popExitedContainer();

        if (allocContainer == null){
            log.info("exited상태인 컨테이너가 존재하지 않습니디. 컨태이너 추가");
            prepareContainers();
            allocContainer = containerRepository.popExitedContainer();
        }

        runContainer(allocContainer);
        return containerRepository.saveActiveContainer(userUUID, allocContainer);
    }

    @Override
    public void stopContainer(String userUUID) throws IOException {
        MyContainer stopContainer = containerRepository.popActiveContainer(userUUID);

        if(stopContainer == null){
            log.info("[DockerService#stopContainer] can not found active container. userUUID = {}", userUUID);
            return;
        }

        containerRepository.saveExitedContainer(stopContainer);
        dockerClient.stopContainerCmd(stopContainer.getContainerId())
                .exec();
    }

    @Override
    public void deleteContainer() {

    }

    private void close(String userUUID) {
        /* TDD
         * docker container 종료 or 삭제
         */

        //containerRepository.deleteContainer(userUUID);
    }

    private void sendMessage(WebSocketSession session, byte[] buffer) throws IOException {
        session.sendMessage(new TextMessage(buffer));
    }
}
