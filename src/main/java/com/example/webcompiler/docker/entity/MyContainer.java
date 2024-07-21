package com.example.webcompiler.docker.entity;

import lombok.Getter;
import lombok.Setter;

import java.io.*;
import java.time.LocalDateTime;

@Getter
@Setter
public class MyContainer {
    String containerId;
    String containerName;
    Integer publicPort;
    LocalDateTime lastUsed;
    InputStream inputStream = null;
    OutputStream outputStream = null;

    public MyContainer(final String containerId) throws IOException {

        this.containerId = containerId;
        setStream();
    }

    public MyContainer(String containerId, String containerName, LocalDateTime lastUsed) throws IOException {
        this.containerId = containerId;
        this.containerName = containerName;
        this.lastUsed = lastUsed;
        setStream();
    }

    private void setStream() throws IOException {
        this.outputStream = new PipedOutputStream();
        this.inputStream = new PipedInputStream((PipedOutputStream) this.outputStream);
    }
}
