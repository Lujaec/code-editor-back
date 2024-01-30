package com.example.webcompiler.file.presentation.dto.request;

import com.example.webcompiler.file.domain.Extension;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FileExecuteRequest {
    String content;
    Extension extension;
}
