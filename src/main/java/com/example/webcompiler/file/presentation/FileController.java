package com.example.webcompiler.file.presentation;

import com.example.webcompiler.auth.presentation.AuthenticationPrincipal;
import com.example.webcompiler.file.application.FileService;
import com.example.webcompiler.file.application.dto.FileCreateDto;
import com.example.webcompiler.file.application.dto.FileDeleteDto;
import com.example.webcompiler.file.application.dto.FileExecuteDto;
import com.example.webcompiler.file.application.dto.FileUpdateDto;
import com.example.webcompiler.file.presentation.dto.request.FileCreateRequest;
import com.example.webcompiler.file.presentation.dto.request.FileExecuteRequest;
import com.example.webcompiler.file.presentation.dto.request.FileUpdateRequest;
import com.example.webcompiler.file.presentation.dto.response.FileInfoResponse;
import com.example.webcompiler.ssh.application.SshService;
import com.example.webcompiler.ssh.domain.SshConnection;
import com.example.webcompiler.user.domain.User;
import com.example.webcompiler.utill.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Slf4j
public class FileController {
    private final ModelMapper mapper;
    private final FileService fileService;
    private final SshService sshService;

    @PostMapping()
    public ResponseEntity<ApiResponse<FileInfoResponse>> create(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody FileCreateRequest request
            ){
        FileCreateDto dto = mapper.map(request, FileCreateDto.class);
        dto.setUserId(user.getId());

        FileInfoResponse fileInfoResponse = fileService.create(dto);
        return ResponseEntity.ok(ApiResponse.success(fileInfoResponse));
    }

    @GetMapping("/{fileUUID}")
    public ResponseEntity<ApiResponse<FileInfoResponse>> findByFileUUID(
            @AuthenticationPrincipal User user,
            @PathVariable String fileUUID
    ){
        FileInfoResponse fileInfoResponse = fileService.findByUUID(fileUUID);
        return ResponseEntity.ok(ApiResponse.success(fileInfoResponse));
    }

    @PatchMapping("/{fileUUID}")
    public ResponseEntity<ApiResponse<FileInfoResponse>> update(
            @AuthenticationPrincipal User user,
            @PathVariable String fileUUID,
            @Valid @RequestBody FileUpdateRequest request
            ){
        FileUpdateDto dto = mapper.map(request, FileUpdateDto.class);
        dto.setFileUUID(fileUUID);

        FileInfoResponse fileInfoResponse = fileService.update(dto);
        return ResponseEntity.ok(ApiResponse.success(fileInfoResponse));
    }

    @DeleteMapping("/{fileUUID}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @AuthenticationPrincipal User user,
            @PathVariable String fileUUID
    ){
        FileDeleteDto dto = new FileDeleteDto(fileUUID);

        fileService.delete(dto);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/run")
    public ResponseEntity<ApiResponse<Void>> run(
            @AuthenticationPrincipal User user,
            @RequestBody FileExecuteRequest request
    ) throws IOException {

        FileExecuteDto dto = mapper.map(request, FileExecuteDto.class);

        fileService.execute(dto);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
