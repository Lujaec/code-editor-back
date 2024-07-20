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
    * inActive -> Non Active를 의미
    * */
    private static List<MyContainer> inActiveStore = new CopyOnWriteArrayList<>();
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
    public MyContainer getInActiveContainer() {
        if (inActiveStore.isEmpty())
            return null;

        return inActiveStore.get(0);
    }

    @Override
    public MyContainer popInActiveContainer() {
        if (inActiveStore.isEmpty())
            return null;
        MyContainer myContainer = inActiveStore.get(inActiveStore.size() - 1);
        inActiveStore.remove(inActiveStore.size() - 1);
        return myContainer;
    }

}
