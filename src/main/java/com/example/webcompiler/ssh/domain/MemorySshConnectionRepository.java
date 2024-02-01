package com.example.webcompiler.ssh.domain;

import com.example.webcompiler.ssh.domain.SshConnection;
import org.springframework.stereotype.Repository;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class MemorySshConnectionRepository {
    private static Map<String, SshConnection> store = new ConcurrentHashMap<>();

    public SshConnection getSshConnection(String webSocketSessionId) {
        return store.get(webSocketSessionId);
    }


    public void saveSshConnection(String webSocketSessionId, SshConnection sshConnection) throws IOException {
        store.put(webSocketSessionId, sshConnection);
    }


    public void deleteSshConnection(WebSocketSession webSocketSession) {
        store.remove(webSocketSession);
    }
}
