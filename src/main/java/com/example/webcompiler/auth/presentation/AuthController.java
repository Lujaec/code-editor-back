package com.example.webcompiler.auth.presentation;

import com.example.webcompiler.auth.application.AuthService;
import com.example.webcompiler.auth.dto.request.SignInDto;
import com.example.webcompiler.auth.dto.request.SignUpDto;
import com.example.webcompiler.auth.dto.response.AuthDto;
import com.example.webcompiler.auth.dto.response.UserInfoDto;
import com.example.webcompiler.utill.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    private final AuthService authService;

    @GetMapping("/healthCheck")
    public String healthCheck(){
        return "Connect !";
    }

    @PostMapping("")
    public ResponseEntity<ApiResponse<UserInfoDto>> signUp(
            @Valid @RequestBody SignUpDto signUpDto
    ) {
        UserInfoDto userInfoDto = authService.signUp(signUpDto);
        return ResponseEntity.ok(ApiResponse.success(userInfoDto));
    }

    @PostMapping("/signIn")
    public ResponseEntity<ApiResponse<AuthDto>> signIn(
            @Valid @RequestBody SignInDto signInDto
    ) {
        AuthDto authDto = authService.signIn(signInDto);
        return ResponseEntity.ok(ApiResponse.success(authDto));
    }
}
