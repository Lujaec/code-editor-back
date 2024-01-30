package com.example.webcompiler.ssh.repository;

import com.example.webcompiler.ssh.entity.SshConnection;
import org.springframework.stereotype.Repository;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class MemorySshConnectionRepository {
    private static Map<String, SshConnection> store = new ConcurrentHashMap<>();

    public SshConnection getContainer(WebSocketSession session) {
        return store.get(session.getId());
    }


    public void saveContainer(WebSocketSession session, SshConnection sshConnection) throws IOException {
        store.put(session.getId(), sshConnection);
    }


    public void deleteContainer(String userUUID) {
        store.remove(userUUID);
    }
}
