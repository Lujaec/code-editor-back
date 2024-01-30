package com.example.webcompiler.file.application.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FileDeleteDto {
    private String fileUUID;

    public FileDeleteDto(String fileUUID) {
        this.fileUUID = fileUUID;
    }
}
