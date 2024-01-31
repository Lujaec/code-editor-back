package com.example.webcompiler.webSocket.dto;

import lombok.Data;

@Data
public class TerminalConnectionDto {
    private String	type;
    private String 	host;
    private int	port;
    private String	username;
    private String	password;
    private String userUUID;
}
