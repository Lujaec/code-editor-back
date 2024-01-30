package com.example.webcompiler.ssh.dto;

import lombok.Data;

@Data
public class SshConnectionDto {
    private String	type;
    private String 	host;
    private String containerId;
    private int	port;
}
