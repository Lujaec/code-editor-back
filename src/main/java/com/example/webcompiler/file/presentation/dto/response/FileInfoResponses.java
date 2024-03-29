package com.example.webcompiler.file.presentation.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class FileInfoResponses {
    private List<FileInfoResponse> list;

    public FileInfoResponses(List<FileInfoResponse> list) {
        this.list = list;
    }
}
