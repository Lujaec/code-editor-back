package com.example.webcompiler.webSocket.handler;

import com.example.webcompiler.docker.entity.MyContainer;
import com.example.webcompiler.docker.service.DockerService;
import com.example.webcompiler.webSocket.dto.TerminalCommandDto;
import com.example.webcompiler.webSocket.dto.TerminalConnectionDto;
import com.example.webcompiler.ssh.application.dto.SshConnectionDto;
import com.example.webcompiler.ssh.application.SshService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.sockjs.transport.session.WebSocketServerSockJsSession;

@Slf4j
@Component
@RequiredArgsConstructor
public class TerminalHandler implements WebSocketHandler {
    private final DockerService dockerService;
    private final SshService sshService;
    private final ModelMapper mapper;

    @Value("${session.connection.prefix}")
    private String sessionConnectionPrefix;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        if(!(session instanceof WebSocketServerSockJsSession))
            return;

        log.info("success connection. sessionId = {}", session.getId());
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        ObjectMapper om = new ObjectMapper();

        log.info("handle Message. session Id = {}", session.getId());

        if(message.getPayload().toString().contains("OPEN WEB SOCKET")) {
            TerminalConnectionDto terminalConnectionDto = om.readValue(message.getPayload().toString(), TerminalConnectionDto.class);
            MyContainer myContainer = dockerService.allocateContainer(terminalConnectionDto.getUserUUID());

            SshConnectionDto connectionDto = mapper.map(terminalConnectionDto, SshConnectionDto.class);
            connectionDto.setPort(myContainer.getPublicPort());
            connectionDto.setContainerId(myContainer.getContainerId());

            sshService.initConnection(session, connectionDto);
            session.sendMessage(new TextMessage(sessionConnectionPrefix + session.getId()));
        }else{
            TerminalCommandDto terminalCommandDto = om.readValue(message.getPayload().toString(), TerminalCommandDto.class);
            sshService.receiveHandle(session, terminalCommandDto.getCommand());
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        sshService.close(session.getId());
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
}
