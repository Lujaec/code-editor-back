package com.example.webcompiler.ssh.service;

import com.example.webcompiler.ssh.dto.SshConnectionDto;
import com.example.webcompiler.ssh.entity.SshConnection;
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
    private static Map<WebSocketSession, SshConnection> store = new ConcurrentHashMap<>();
    private final ModelMapper mapper;

    @Value("${ec2.info.host}")
    private String host;

    @Value("${ec2.info.username}")
    private String username;

    @Value("${ec2.info.password}")
    private String password;

    private ExecutorService executorService = Executors.newCachedThreadPool();

    public void initConnection(WebSocketSession webSocketSession, SshConnectionDto dto) {
        JSch jsch = new JSch();

        SshConnection connection = mapper.map(dto, SshConnection.class);
        connection.setRemotePort(dto.getPort());
        connection.setJsch(jsch);
        connection.setWebSocketSession(webSocketSession);

        store.put(webSocketSession, connection);

        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    connectToSSH(connection, webSocketSession);
                } catch (JSchException | IOException e) {
                    log.error("error: {}", e);
                    close(webSocketSession);
                }
            }
        });
    }

    public void receiveHandle(WebSocketSession session, String command) {
        SshConnection connection = store.get(session);

        if (connection != null) {
            try {
                transToSSh(connection, command);
            } catch (IOException e) {
                log.error("에러 정보: {}", e);
                close(session);
            }
        }
    }

    public void sendMessage(WebSocketSession session, byte[] buffer) throws IOException {
        session.sendMessage(new TextMessage(buffer));
    }

    public void close(WebSocketSession webSocketSession) {
        SshConnection connection =  store.get(webSocketSession);

        if (connection != null) {
            if (connection.getChannel() != null) connection.getChannel().disconnect();
        }
    }

    private void connectToSSH(SshConnection connection, WebSocketSession webSocketSession) throws JSchException, IOException {
        final String STATEMENT_DOCKER_EXECUTE = "sudo docker exec -it " + connection.getContainerId() + " /bin/bash\n";

        Session session1 = null;
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");

        session1 = connection.getJsch().getSession(username, host, 22);
        session1.setPassword(password);
        session1.setConfig(config);
        session1.connect(60000);
        Channel channel = session1.openChannel("shell");
        channel.connect();
        InputStream is = channel.getInputStream();

        connection.setChannel(channel);

        transToSSh(connection, STATEMENT_DOCKER_EXECUTE);
        transToSSh(connection, "clear\n");

        try {
            byte[] buffer = new byte[1024];
            int i = 0;
            while((i = is.read(buffer)) != -1) {
                sendMessage(webSocketSession, Arrays.copyOfRange(buffer, 0, i));
            }
        } finally {
            session1.disconnect();

            if (is != null) {
                is.close();
            }
        }


    }

    private void transToSSh(SshConnection connection, String command) throws IOException {
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
}
