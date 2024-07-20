package com.example.webcompiler.docker.repository;

import com.example.webcompiler.docker.entity.MyContainer;

import java.io.IOException;

public interface ContainerRepository {
    MyContainer getActiveContainer(String userUUID);

    MyContainer saveActiveContainer(String userUUID, MyContainer myContainer);

    MyContainer popActiveContainer(String userUUID);

    MyContainer getInActiveContainer();

    MyContainer saveInActiveContainer(MyContainer myContainer) throws IOException;

    MyContainer popInActiveContainer();

}
