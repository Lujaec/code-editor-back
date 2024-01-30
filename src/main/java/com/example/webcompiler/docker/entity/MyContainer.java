package com.example.webcompiler.docker.entity;

import lombok.Getter;
import lombok.Setter;

import java.io.*;

@Getter
@Setter
public class MyContainer {
    String containerId;
    String containerName;
    Integer publicPort;
    InputStream inputStream = null;
    OutputStream outputStream = null;

    public MyContainer(final String containerId) throws IOException {

        this.containerId = containerId;
        setStream();
    }

    public MyContainer(String containerId, String containerName) throws IOException {
        this.containerId = containerId;
        this.containerName = containerName;
        setStream();
    }

    private void setStream() throws IOException {
        this.outputStream = new PipedOutputStream();
        this.inputStream = new PipedInputStream((PipedOutputStream) this.outputStream);
    }
}
