package com.example.webcompiler.webSocket.dto;

import lombok.Data;

@Data
public class TerminalDto {
    private String	type;
    private String 	host;
    private int	port;
    private String	username;
    private String	password;
}
