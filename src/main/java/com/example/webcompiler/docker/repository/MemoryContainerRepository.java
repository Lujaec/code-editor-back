package com.example.webcompiler.docker.repository;

import com.example.webcompiler.docker.entity.MyContainer;
import org.springframework.stereotype.Repository;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class MemoryContainerRepository implements ContainerRepository{
    private static Map<String, MyContainer> store = new ConcurrentHashMap<>();

    @Override
    public MyContainer getContainer(String userUUID) {
        return store.get(userUUID);
    }

    @Override
    public void saveContainer(String userUUID, MyContainer myContainer) throws IOException {
        store.put(userUUID, myContainer);
    }

    @Override
    public void deleteContainer(String userUUID) {
        store.remove(userUUID);
    }
}
