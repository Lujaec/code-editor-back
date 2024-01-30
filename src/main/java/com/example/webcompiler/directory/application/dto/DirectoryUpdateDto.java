package com.example.webcompiler.directory.application.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DirectoryUpdateDto {
    private String title;
    private String explanation;
    private String directoryUUID;
}
