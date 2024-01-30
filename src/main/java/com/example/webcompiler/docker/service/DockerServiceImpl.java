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

    @Override
    public MyContainer createContainer(WebSocketSession session) throws IOException {
        UUID uuid = UUID.randomUUID();

        ExposedPort exposedPort = ExposedPort.tcp(22);
        Ports portBindings = new Ports();
        portBindings.bind(exposedPort, Ports.Binding.bindPort(0)); // 나중에 0으로 바꿔야함

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
        containerRepository.saveContainer(session, createdMyContainer);
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
    public void readContainerOutput(MyContainer myContainer, WebSocketSession session) throws IOException {
        ExecCreateCmdResponse execCreateCmdResponse = dockerClient.execCreateCmd(myContainer.getContainerId())
                .withAttachStdout(true)
                .withAttachStdin(true)
                .withAttachStderr(true)
                .withTty(true)
                .withCmd("/bin/bash")
                .exec();

        InputStream inputStream = myContainer.getInputStream();
        OutputStream outputStream = myContainer.getOutputStream();

        dockerClient.execStartCmd(execCreateCmdResponse.getId())
                .withTty(true)
                .withStdIn(inputStream)
                .exec(new ExecStartResultCallback(outputStream, System.err));

        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    try {
                        byte[] buffer = new byte[1024];
                        int i = 0;
                        while((i = inputStream.read(buffer)) != -1) {
                            log.info("send message: {}", new String(buffer, 0, i));
                            sendMessage(session, Arrays.copyOfRange(buffer, 0, i));
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    } finally {
                        if (inputStream != null) {
                            inputStream.close();
                        }
                    }
                } catch (IOException e) {
                    log.error("error: {}", e);
                    close(session);
                }
            }
        });
    }

    @Override
    public void writeContainer(WebSocketSession session, String command) throws IOException {
        MyContainer myContainer = containerRepository.getContainer(session);



        OutputStream outputStream = myContainer.getOutputStream();
        if (command.equals("SIGINT")) {
            outputStream.write(3);
        } else if(command.equals("SIGTSTP")) {
            outputStream.write(26);
        } else {
            outputStream.write(command.getBytes());
        }
        outputStream.flush();
    }


    @Override
    public void stopContainer() {

    }

    @Override
    public void deleteContainer() {

    }

    private void close(WebSocketSession session) {
        /* TDD
         * docker container 종료 or 삭제
         */

        containerRepository.deleteContainer(session);
    }

    private void sendMessage(WebSocketSession session, byte[] buffer) throws IOException {
        session.sendMessage(new TextMessage(buffer));
    }
}
