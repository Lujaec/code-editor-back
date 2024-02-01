package com.example.webcompiler.file.application.dto;

import com.example.webcompiler.file.domain.Extension;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class FileExecuteDto {
    private String title;
    private String content;
    private Extension extension;
    private String webSocketSessionId;
}
