package com.example.webcompiler.ssh.domain;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
@Slf4j
public class MemorySshConnectionRepository {
    /**
     * key: webSocketSessionID
     * value: SshConnection
     */
    private static Map<String, SshConnection> connectionStore = new ConcurrentHashMap<>();

    /**
     * key: userUUID
     * Value: userUUID를 가진 사용자가 현재 연결한 SSH 개수
     */
    private static Map<String, Integer> sshCntStore = new ConcurrentHashMap<>();

    public SshConnection getSshConnection(String webSocketSessionId) {
        return connectionStore.get(webSocketSessionId);
    }

    public SshConnection saveSshConnection(String webSocketSessionId, SshConnection sshConnection ){
        connectionStore.put(webSocketSessionId, sshConnection);
        return sshConnection;
    }

    public void deleteSshConnection(String webSocketSessionId){
        connectionStore.remove(webSocketSessionId);
    }

    public int updateSshCnt(String userUUID, int delta){
        int cnt = sshCntStore.merge(userUUID, delta, Integer::sum);

        if (cnt == 0)
            sshCntStore.remove(userUUID);

        return cnt;
    }
}
