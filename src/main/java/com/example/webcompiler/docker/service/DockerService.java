package com.example.webcompiler.docker.service;

import com.example.webcompiler.docker.entity.MyContainer;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

public interface DockerService {
    public MyContainer createContainer(String userUUID) throws IOException;

    public void runContainer(MyContainer myContainer) throws InterruptedException;

    public void stopContainer();
    public void deleteContainer();
}
