package com.example.webcompiler.user.application;

import com.example.webcompiler.user.domain.User;
import com.example.webcompiler.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;

    public User getById(Long id){
        Optional<User> byId = userRepository.findById(id);

        if(byId.isEmpty()){
            log.info("id = {} 인 사용자가 존재하지 않습니다", id);
            throw new IllegalArgumentException();
        }

        return byId.get();
    }


}
