package com.example.webcompiler.directory.presentation.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DirectoryCreateRequest {
    @NotEmpty
    private String title;
    @NotNull
    private String explanation;
}
