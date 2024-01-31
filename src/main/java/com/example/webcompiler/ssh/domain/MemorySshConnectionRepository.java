package com.example.webcompiler.ssh.domain;

import com.example.webcompiler.ssh.domain.SshConnection;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class MemorySshConnectionRepository {
    private static Map<String, SshConnection> store = new ConcurrentHashMap<>();

    public SshConnection getContainer(String userUUID) {
        return store.get(userUUID);
    }


    public void saveContainer(String userUUID, SshConnection sshConnection) throws IOException {
        store.put(userUUID, sshConnection);
    }


    public void deleteContainer(String userUUID) {
        store.remove(userUUID);
    }
}
