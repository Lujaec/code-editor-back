package com.example.webcompiler.auth.application;

import com.example.webcompiler.auth.dto.request.SignInDto;
import com.example.webcompiler.auth.dto.response.AuthDto;
import com.example.webcompiler.auth.dto.request.SignUpDto;
import com.example.webcompiler.auth.dto.response.UserInfoDto;
import com.example.webcompiler.user.domain.User;
import com.example.webcompiler.user.domain.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AuthService {
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final ModelMapper mapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserInfoDto signUp(SignUpDto dto){
        User user = mapper.map(dto, User.class);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));

        user.setUserUUID(UUID.randomUUID().toString());

        User saved = userRepository.save(user);
        return mapper.map(saved, UserInfoDto.class);
    }

    public AuthDto signIn(SignInDto dto){
        final String email = dto.getEmail();
        final String password = dto.getPassword();

        Optional<User> userWrapper = userRepository.findByEmail(email);

        if(userWrapper.isEmpty()){
            log.info("{} 에 해당하는 사용자가 없습니다.", email);
            throw new IllegalArgumentException("email not found");
        }

        User user = userWrapper.get();

        if(!passwordEncoder.matches(password, user.getPassword())){
            log.info("패스워드가 일치하지 않습니다. email = {}, pwd = {}", email, password);
            throw new IllegalArgumentException("email not found");
        }

        return new AuthDto(user.getUserUUID(), jwtService.createAccessToken(user.getId()));
    }
}
