package com.example.webcompiler.webSocket.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TerminalCommandDto {
    private String sessionId;
    private String command;
}
