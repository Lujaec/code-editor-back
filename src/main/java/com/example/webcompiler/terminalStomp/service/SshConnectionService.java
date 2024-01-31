//package com.example.websocket.terminalStomp.service;
//
//import com.example.websocket.terminalStomp.dto.SshConnectionDto;
//import com.example.websocket.terminalStomp.dto.TerminalConnectionDto;
//import com.jcraft.jsch.Channel;
//import com.jcraft.jsch.JSch;
//import com.jcraft.jsch.JSchException;
//import com.jcraft.jsch.Session;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//import org.springframework.web.socket.TextMessage;
//import org.springframework.web.socket.WebSocketSession;
//
//import java.io.*;
//import java.util.Arrays;
//import java.util.Map;
//import java.util.Properties;
//import java.util.concurrent.*;
//
//@Service
//@Slf4j
//@RequiredArgsConstructor
//public class SshService {
//    private static Map<String, Object> sshMap = new ConcurrentHashMap<>();
//
//    private ExecutorService executorService = Executors.newCachedThreadPool();
//    private final TerminalService terminalService;
//
//    public String initConnection(TerminalConnectionDto dto) throws ExecutionException, InterruptedException {
//        JSch jsch = new JSch();
//        SshConnectionDto connectionDto = new SshConnectionDto();
//        connectionDto.setJsch(jsch);
//        connectionDto.setInfo(dto);
//        sshMap.put(dto.getTerminalUUID(), connectionDto);
//
//        Future<String> future = executorService.submit(new Callable<String>() {
//            @Override
//            public String call() {
//                try {
//                    // 여기에서 실제 작업 수행
//                    return connectToSSH(connectionDto, dto);
//                } catch (JSchException | IOException e) {
//                    log.error("error: {}", e);
//                    return "Connection Fail";
//                }
//            }
//        });
//
//        return future.get();
//    }
//
//    public void receiveHandle(WebSocketSession session, String command) {
//        SshConnectionDto connectionDto = (SshConnectionDto) sshMap.get(session);
//
//        if (connectionDto != null) {
//            try {
//                transToSSh(connectionDto.getChannel(), command);
//            } catch (IOException e) {
//                log.error("에러 정보: {}", e);
//                close(session);
//            }
//        }
//    }
//
//    public void close(WebSocketSession session) {
//        SshConnectionDto connectionDto = (SshConnectionDto) sshMap.get(session);
//        if (connectionDto != null) {
//            if (connectionDto.getChannel() != null) connectionDto.getChannel().disconnect();
//            sshMap.remove(session);
//        }
//    }
//
//    private String connectToSSH(SshConnectionDto connectionDto, TerminalConnectionDto dto) throws JSchException, IOException {
//        Session session = null;
//        String sshMessage = "";
//        Properties config = new Properties();
//        config.put("StrictHostKeyChecking", "no");
//
//        session = connectionDto.getJsch().getSession(dto.getUsername(), dto.getHost(), dto.getPort());
//        session.setConfig(config);
//
//        session.setPassword(dto.getPassword());
//        session.connect(60000);
//
//        Channel channel = session.openChannel("shell");
//
//        channel.connect(3000);
//        connectionDto.setChannel(channel);
//
//        InputStream is = channel.getInputStream();
//        StringBuilder messageBuilder = new StringBuilder();
//        try {
//            byte[] buffer = new byte[1024];
//            int i = 0;
//            is.read(buffer, 0, 1024);
//            messageBuilder.append(new String(buffer));
//
//            log.info("connectToSSh completed !!");
//            return messageBuilder.toString();
//        }
//        catch(Exception e){
//            log.info("connectToSSH: fail");
//            return "Connection Fail";
//        }
//        finally {
//            session.disconnect();
//            channel.disconnect();
//            if (is != null) {
//                is.close();
//            }
//        }
//    }
//
//    private void transToSSh(Channel channel, String command) throws IOException {
//        if (channel != null) {
//            OutputStream os = channel.getOutputStream();
//            if (command.equals("SIGINT")) {
//                os.write(3);
//            } else if(command.equals("SIGTSTP")) {
//                os.write(26);
//            } else {
//                os.write(command.getBytes());
//            }
//            os.flush();
//        }
//    }
//}
