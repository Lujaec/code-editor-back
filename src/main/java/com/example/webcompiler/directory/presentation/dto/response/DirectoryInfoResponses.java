package com.example.webcompiler.directory.presentation.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DirectoryInfoResponses {
    List<DirectoryInfoResponse> info;

    public DirectoryInfoResponses(List<DirectoryInfoResponse> info) {
        this.info
                = info;
    }
}
