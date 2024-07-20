package com.example.webcompiler.docker.repository;

import com.example.webcompiler.docker.entity.MyContainer;

import java.io.IOException;
import java.util.Deque;
import java.util.List;

public interface ContainerRepository {
    MyContainer getActiveContainer(String userUUID);

    MyContainer saveActiveContainer(String userUUID, MyContainer myContainer);

    MyContainer popActiveContainer(String userUUID);

    Deque<MyContainer> getInActiveContainers();

    MyContainer saveInActiveContainer(MyContainer myContainer) throws IOException;

    MyContainer popInActiveContainer();

  void removeInActiveContainer(String containerId);
}
