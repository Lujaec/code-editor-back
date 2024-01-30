package com.example.webcompiler.file.presentation.dto.response;

import com.example.webcompiler.file.domain.Extension;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FileInfoResponse {
    private String title;
    private String content;
    private Extension extension;
    private String fileUUID;
    private String directoryUUID;
}
