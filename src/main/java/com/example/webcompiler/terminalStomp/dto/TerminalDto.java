package com.example.webcompiler.terminalStomp.dto;

import lombok.Data;

@Data
public class TerminalDto {
    private String	type;
    private String terminalUUID;
    private String 	host;
    private int	port;
    private String	username;
    private String	password;
}
