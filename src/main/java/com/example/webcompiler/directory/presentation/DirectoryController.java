package com.example.webcompiler.directory.presentation;

import com.example.webcompiler.auth.presentation.AuthenticationPrincipal;
import com.example.webcompiler.directory.application.DirectoryService;
import com.example.webcompiler.directory.application.dto.DirectoryCreateDto;
import com.example.webcompiler.directory.application.dto.DirectoryDeleteDto;
import com.example.webcompiler.directory.application.dto.DirectoryUpdateDto;
import com.example.webcompiler.directory.presentation.dto.request.DirectoryCreateRequest;
import com.example.webcompiler.directory.presentation.dto.request.DirectoryUpdateRequest;
import com.example.webcompiler.directory.presentation.dto.response.DirectoryInfoResponse;
import com.example.webcompiler.directory.presentation.dto.response.DirectoryInfoResponses;
import com.example.webcompiler.user.domain.User;
import com.example.webcompiler.utill.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/directories")
@RequiredArgsConstructor
public class DirectoryController {
    private final DirectoryService directoryService;
    private final ModelMapper mapper;

    @PostMapping()
    public ResponseEntity<ApiResponse<DirectoryInfoResponse>> create(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody DirectoryCreateRequest request
            ){

        DirectoryCreateDto dto = mapper.map(request, DirectoryCreateDto.class);
        dto.setUserId(user.getId());
        DirectoryInfoResponse directoryInfoResponse = directoryService.create(dto);
        return ResponseEntity.ok(ApiResponse.success(directoryInfoResponse));
    }

    @GetMapping()
    public ResponseEntity<ApiResponse<DirectoryInfoResponses>> findByUserId(
            @AuthenticationPrincipal User user){
        DirectoryInfoResponses infoResponses = directoryService.findByUserId(user.getId());
        return ResponseEntity.ok(ApiResponse.success(infoResponses));
    }

    @GetMapping("/{directoryUUID}")
    public ResponseEntity<ApiResponse<DirectoryInfoResponse>> findById(
            @AuthenticationPrincipal User user,
            @PathVariable String directoryUUID
    ){
        DirectoryInfoResponse infoResponse = directoryService.findByUUID(directoryUUID);
        return ResponseEntity.ok(ApiResponse.success(infoResponse));
    }

    @PatchMapping("/{directoryUUID}")
    public ResponseEntity<ApiResponse<DirectoryInfoResponse>> update(
            @AuthenticationPrincipal User user,
            @PathVariable String directoryUUID,
            @Valid @RequestBody DirectoryUpdateRequest request
    ){
        DirectoryUpdateDto dto = mapper.map(request, DirectoryUpdateDto.class);
        DirectoryInfoResponse infoResponse = directoryService.update(dto);
        return ResponseEntity.ok(ApiResponse.success(infoResponse));
    }

    @DeleteMapping("/{directoryUUID}")
    public ResponseEntity<ApiResponse<Void>> deleteById(
            @AuthenticationPrincipal User user,
            @PathVariable String directoryUUID
            ){

        DirectoryDeleteDto dto = new DirectoryDeleteDto(directoryUUID);
        directoryService.delete(dto);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}