package com.example.webcompiler.webSocket.handler;

import com.example.webcompiler.docker.entity.MyContainer;
import com.example.webcompiler.docker.service.DockerService;
import com.example.webcompiler.webSocket.dto.TerminalDto;
import com.example.webcompiler.ssh.dto.SshConnectionDto;
import com.example.webcompiler.ssh.service.SshService;
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
            TerminalDto terminalDto = om.readValue(message.getPayload().toString(), TerminalDto.class);

            MyContainer myContainer = dockerService.createContainer(session);
            dockerService.runContainer(myContainer);

            SshConnectionDto connectionDto = mapper.map(terminalDto, SshConnectionDto.class);
            connectionDto.setPort(myContainer.getPublicPort());
            connectionDto.setContainerId(myContainer.getContainerId());

            sshService.initConnection(session, connectionDto);
            //dockerService.readContainerOutput(myContainer, session);

        }else{
            sshService.receiveHandle(session, message.getPayload().toString());

            //String command = message.getPayload().toString();
            //dockerService.writeContainer(session, command);
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
