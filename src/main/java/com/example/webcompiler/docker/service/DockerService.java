package com.example.webcompiler.docker.service;

import com.example.webcompiler.docker.entity.MyContainer;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

public interface DockerService {
    public MyContainer createContainer(WebSocketSession session) throws IOException;

    public void runContainer(MyContainer myContainer) throws InterruptedException;

    public void readContainerOutput(MyContainer myContainer, WebSocketSession session) throws IOException;

    public void writeContainer(WebSocketSession session, String command) throws IOException;
    public void stopContainer();
    public void deleteContainer();
}
