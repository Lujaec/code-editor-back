package com.example.webcompiler.ssh.entity;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import lombok.Data;
import org.springframework.web.socket.WebSocketSession;

import java.net.Socket;

@Data
public class SshConnection {
    private String type;
    private String containerId;
    private int	remotePort;
    private int localPort;
    private WebSocketSession webSocketSession;
    private JSch jsch;
    private Channel channel;
    private Socket socket;
}
