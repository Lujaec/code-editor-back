package com.example.webcompiler.directory.presentation.dto.response;

import com.example.webcompiler.file.presentation.dto.response.FileInfoResponse;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DirectoryInfoResponse {
    private String title;
    private String explanation;
    private String directoryUUID;
    private List<FileInfoResponse> filInfos;
}
