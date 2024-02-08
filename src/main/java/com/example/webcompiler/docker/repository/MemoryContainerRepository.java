package com.example.webcompiler.docker.repository;

import com.example.webcompiler.docker.entity.MyContainer;
import org.springframework.stereotype.Repository;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Repository
public class MemoryContainerRepository implements ContainerRepository{
    /**
    * exited -> Non Active를 의미
    * */
    private static List<MyContainer> exitedStore = new CopyOnWriteArrayList<>();
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
    public MyContainer saveExitedContainer(MyContainer myContainer) throws IOException {
        if(exitedStore.add(myContainer))
            return myContainer;
        else
            return null;
    }

    @Override
    public MyContainer getExitedContainer() {
        if (exitedStore.isEmpty())
            return null;

        return exitedStore.get(0);
    }

    @Override
    public MyContainer popExitedContainer() {
        if (exitedStore.isEmpty())
            return null;
        MyContainer myContainer = exitedStore.get(exitedStore.size() - 1);
        exitedStore.remove(exitedStore.size() - 1);
        return myContainer;
    }

}
