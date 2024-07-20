package com.example.webcompiler.docker.entity;

public enum ContainerStatus {
    CREATED("created"),
    RESTARTING("restarting"),
    RUNNING("running"),
    REMOVING("removing"),
    PAUSED("paused"),
    EXITED("exited"),
    DEAD("dead"),
    UNKNOWN("unknown");

    private final String status;

    ContainerStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public static ContainerStatus fromString(String status) {
        for (ContainerStatus s : ContainerStatus.values()) {
            if (s.getStatus().equalsIgnoreCase(status)) {
                return s;
            }
        }

        return UNKNOWN;
    }
}