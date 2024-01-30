package com.example.webcompiler.directory.application.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DirectoryDeleteDto {
    private String directoryUUID;

    public DirectoryDeleteDto(String directoryUUID) {
        this.directoryUUID = directoryUUID;
    }
}
