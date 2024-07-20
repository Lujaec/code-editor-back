package com.example.webcompiler.docker.repository;

import com.example.webcompiler.docker.entity.MyContainer;
import org.springframework.stereotype.Repository;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.CopyOnWriteArrayList;

@Repository
public class MemoryContainerRepository implements ContainerRepository{
    /**
    * inActive -> Non Active를 의미
    * */
    private static Deque<MyContainer> inActiveStore = new ConcurrentLinkedDeque<>();
    private static Map<String, MyContainer> activeStore = new ConcurrentHashMap<>();

    @Override
    public MyContainer getActiveContainer(String userUUID) {
        MyContainer myContainer = activeStore.get(userUUID);

        return myContainer;
    }

    @Override
    public MyContainer saveActiveContainer(String userUUID, MyContainer myContainer) {
        activeStore.put(userUUID, myContainer);
        return myContainer;
    }

    @Override
    public MyContainer popActiveContainer(String userUUID) {
        MyContainer myContainer = activeStore.get(userUUID);

        if (myContainer == null)
            return null;

        activeStore.remove(userUUID);
        return myContainer;
    }

    @Override
    public MyContainer saveInActiveContainer(MyContainer myContainer) throws IOException {
        if(inActiveStore.add(myContainer))
            return myContainer;
        else
            return null;
    }

    @Override
    public Deque<MyContainer> getInActiveContainers() {
        return inActiveStore;
    }

    @Override
    public MyContainer popInActiveContainer() {
        if (inActiveStore.isEmpty())
            return null;

        return inActiveStore.pollLast();
    }

    @Override
    public void removeInActiveContainer(String containerId) {
        inActiveStore.removeIf(container -> container.getContainerId().equals(containerId));
    }
}
