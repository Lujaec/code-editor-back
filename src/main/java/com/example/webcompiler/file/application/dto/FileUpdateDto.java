package com.example.webcompiler.file.application.dto;

import com.example.webcompiler.file.domain.Extension;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class FileUpdateDto {
    private String title;
    private String content;
    private Extension extension;
    private String fileUUID;
}
