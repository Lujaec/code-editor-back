package com.example.webcompiler.webSocket.handler;

import com.example.webcompiler.docker.entity.MyContainer;
import com.example.webcompiler.docker.service.DockerService;
import com.example.webcompiler.webSocket.dto.TerminalConnectionDto;
import com.example.webcompiler.ssh.application.dto.SshConnectionDto;
import com.example.webcompiler.ssh.application.SshService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.sockjs.transport.session.WebSocketServerSockJsSession;

@Slf4j
@Component
@RequiredArgsConstructor
public class TerminalHandler implements WebSocketHandler {
    private final DockerService dockerService;
    private final SshService sshService;
    private final ModelMapper mapper;

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

            MyContainer myContainer = dockerService.createContainer(terminalConnectionDto.getUserUUID());
            dockerService.runContainer(myContainer);

            SshConnectionDto connectionDto = mapper.map(terminalConnectionDto, SshConnectionDto.class);
            connectionDto.setPort(myContainer.getPublicPort());
            connectionDto.setContainerId(myContainer.getContainerId());

            sshService.initConnection(session, connectionDto);
        }else{
            //TerminalCommandDto terminalCommandDto = om.readValue(message.getPayload().toString(), TerminalCommandDto.class);
            sshService.receiveHandle(session, message.getPayload().toString());
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        sshService.close(session);
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
}
