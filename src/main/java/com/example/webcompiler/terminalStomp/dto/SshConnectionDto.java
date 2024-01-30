package com.example.webcompiler.terminalStomp.dto;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SshConnectionDto {
    private JSch jsch;
    private Channel channel;
    private TerminalDto info;
}
