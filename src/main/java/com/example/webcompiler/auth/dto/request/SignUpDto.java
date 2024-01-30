package com.example.webcompiler.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignUpDto {

    @Email
    private String email;

    @Min(8)
    private String password;

    @NotNull
    private String name;
}
