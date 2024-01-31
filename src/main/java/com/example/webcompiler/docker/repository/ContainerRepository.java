package com.example.webcompiler.docker.repository;

import com.example.webcompiler.docker.entity.MyContainer;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

public interface ContainerRepository {
    MyContainer getContainer(String userUUID);

    void saveContainer(String userUUID, MyContainer myContainer) throws IOException;

    void deleteContainer(String userUUID);
}
