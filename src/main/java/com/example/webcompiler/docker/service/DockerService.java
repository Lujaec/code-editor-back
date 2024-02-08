package com.example.webcompiler.docker.service;

import com.example.webcompiler.docker.entity.MyContainer;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

public interface DockerService {

    public void prepareContainers() throws IOException;

    public MyContainer allocateContainer(String userUUID) throws IOException, InterruptedException;
    public void runContainer(MyContainer myContainer) throws InterruptedException;
    public void stopContainer(String userUUID) throws IOException;
    public void deleteContainer();

    // 삭제할 메서드
    public MyContainer createContainer(String userUUID) throws IOException;
}
