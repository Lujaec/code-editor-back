package com.example.webcompiler.file.presentation.dto.request;

import com.example.webcompiler.file.domain.Extension;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FileExecuteRequest {
    private String title;
    private String content;
    private String fileUUID;
    private Extension extension;
    private String webSocketSessionId;
}
