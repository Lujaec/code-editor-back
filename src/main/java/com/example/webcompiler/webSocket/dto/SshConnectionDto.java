package com.example.webcompiler.webSocket.dto;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.socket.WebSocketSession;

@Getter
@Setter
public class SshConnectionDto {
    private WebSocketSession webSocketSession;
    private JSch jsch;
    private Channel channel;
    private TerminalDto info;
}
