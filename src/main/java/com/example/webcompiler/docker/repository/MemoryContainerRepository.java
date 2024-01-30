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
    public MyContainer getContainer(WebSocketSession session) {
        return store.get(session.getId());
    }

    @Override
    public void saveContainer(WebSocketSession session, MyContainer myContainer) throws IOException {
        store.put(session.getId(), myContainer);
    }

    @Override
    public void deleteContainer(WebSocketSession session) {
        store.remove(session.getId());
    }
}
