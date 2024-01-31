package com.example.webcompiler.ssh.domain;

import com.example.webcompiler.ssh.domain.SshConnection;
import org.springframework.stereotype.Repository;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class MemorySshConnectionRepository {
    private static Map<WebSocketSession, SshConnection> store = new ConcurrentHashMap<>();

    public SshConnection getSshConnection(WebSocketSession webSocketSession) {
        return store.get(webSocketSession);
    }


    public void saveSshConnection(WebSocketSession webSocketSession, SshConnection sshConnection) throws IOException {
        store.put(webSocketSession, sshConnection);
    }


    public void deleteSshConnection(WebSocketSession webSocketSession) {
        store.remove(webSocketSession);
    }
}
