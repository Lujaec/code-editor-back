package com.example.webcompiler.ssh.application;

import com.example.webcompiler.docker.service.DockerService;
import com.example.webcompiler.ssh.application.dto.SshConnectionDto;
import com.example.webcompiler.ssh.domain.MemorySshConnectionRepository;
import com.example.webcompiler.ssh.domain.SshConnection;
import com.jcraft.jsch.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.*;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@Slf4j
@RequiredArgsConstructor
public class SshService {
    private final MemorySshConnectionRepository sshConnectionRepository;
    private static Map<WebSocketSession, SshConnection> store = new ConcurrentHashMap<>();
    private final ModelMapper mapper;
    private final Session ec2Session;
    private final DockerService dockerService;

    @Value("${docker.info.host}")
    private String containerHost;
    @Value("${docker.info.username}")
    private String containerUsername;

    @Value("${docker.info.password}")
    private String containerPassword;

    private ExecutorService executorService = Executors.newCachedThreadPool();

    public SshConnection findByWebSocketSessionId(final String webSocketSessionId){
        SshConnection sshConnection = sshConnectionRepository.getSshConnection(webSocketSessionId);

        if(sshConnection == null){
            log.info("유효하지 않은 webSocketSessionId 입니다. webSocketSessionId = {}", webSocketSessionId);
            throw new IllegalArgumentException();
        }

        return sshConnection;
    }

    public void initConnection(WebSocketSession webSocketSession, SshConnectionDto dto) throws IOException {
        JSch jsch = new JSch();

        SshConnection connection = mapper.map(dto, SshConnection.class);
        connection.setRemotePort(dto.getPort());
        connection.setJsch(jsch);

        saveSshConnection(webSocketSession.getId(), connection);
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    connectToSSH(connection, webSocketSession);
                } catch (JSchException | IOException e) {
                    log.error("error: {}", e);
                    try {
                        close(webSocketSession.getId());
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
        });
    }

    public void receiveHandle(WebSocketSession webSocketSession, String command) {
        SshConnection connection = sshConnectionRepository.getSshConnection(webSocketSession.getId());

        if (connection != null) {
            try {
                transToSSh(connection, command);
            } catch (IOException e) {
                log.error("에러 정보: {}", e);
            }
        }
    }

    public void sendMessage(WebSocketSession session, byte[] buffer) throws IOException {
        session.sendMessage(new TextMessage(buffer));
    }

    public void close(String webSocketSessionId) throws IOException {
        SshConnection connection = sshConnectionRepository.getSshConnection(webSocketSessionId);

        if (connection != null) {
            if (connection.getChannel() != null) connection.getChannel().disconnect();
            deleteSshConnection(webSocketSessionId, connection.getUserUUID());
        }else{
            log.info("[SshService#close] connection not found. websocketSessionId = {}", webSocketSessionId);
        }


    }

    private void connectToSSH(SshConnection connection, WebSocketSession webSocketSession) throws JSchException, IOException {
        int portForwardingL = ec2Session.setPortForwardingL(0, containerHost, connection.getRemotePort());
        log.info("매핑된 리모트 포트: {}", connection.getRemotePort());
        log.info("매핑된 로컬 포트: {}", portForwardingL);
        connection.setLocalPort(portForwardingL);

        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");

        Session containerSession = connection.getJsch().getSession(containerUsername, "127.0.0.1", portForwardingL);
        containerSession.setPassword(containerPassword);
        containerSession.setConfig(config);
        containerSession.connect(60000);
        Channel channel = containerSession.openChannel("shell");
        channel.connect();

        InputStream is = channel.getInputStream();
        connection.setChannel(channel);

        transToSSh(connection, "clear\n");

        try {
            byte[] buffer = new byte[1024];
            int i = 0;
            while((i = is.read(buffer)) != -1) {
                sendMessage(webSocketSession, Arrays.copyOfRange(buffer, 0, i));
            }
        } finally {
            containerSession.disconnect();

            if (is != null) {
                is.close();
            }
        }
    }

    public void transToSSh(SshConnection connection, String command) throws IOException {
        Channel channel = connection.getChannel();

        if (channel != null) {
            OutputStream os = channel.getOutputStream();
            if (command.equals("SIGINT")) {
                os.write(3);
            } else if(command.equals("SIGTSTP")) {
                os.write(26);
            } else {
                os.write(command.getBytes());
            }
            os.flush();
        }
    }

    private void saveSshConnection(String webSocketSessionId, SshConnection sshConnection) throws IOException {
        sshConnectionRepository.saveSshConnection(webSocketSessionId, sshConnection);

        String userUUID = sshConnection.getUserUUID();

        int updatedSshCnt = sshConnectionRepository.updateSshCnt(userUUID, 1);
        log.info("saveSshConnection. userUUID = {} sshCnt = {}", userUUID, updatedSshCnt);
    }

    private void deleteSshConnection(String webSocketSessionId, String userUUID) throws IOException {
        sshConnectionRepository.deleteSshConnection(webSocketSessionId);

        int updateSshCnt = sshConnectionRepository.updateSshCnt(userUUID, -1);
        log.info("deleteSshConnection. userUUID = {} sshCnt = {}", userUUID, updateSshCnt);

        if (updateSshCnt == 0)
            dockerService.stopContainer(userUUID);
    }
}
