package com.example.webcompiler.file.presentation.dto.request;

import com.example.webcompiler.file.domain.Extension;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FileCreateRequest {
    @NotEmpty
    private String title;
    private String content;
    @NotNull
    private Extension extension;
    @NotEmpty
    private String directoryUUID;
}
