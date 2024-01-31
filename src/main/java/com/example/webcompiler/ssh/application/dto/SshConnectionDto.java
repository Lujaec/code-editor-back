package com.example.webcompiler.ssh.application.dto;

import lombok.Data;

@Data
public class SshConnectionDto {
    private String	type;
    private String 	host;
    private String containerId;
    private int	port;
    private String userUUID;
}
