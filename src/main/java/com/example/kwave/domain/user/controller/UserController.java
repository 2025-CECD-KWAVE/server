package com.example.kwave.domain.user.controller;

import com.example.kwave.domain.user.dto.request.SignupRequestDto;
import com.example.kwave.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<String> signUpUser (@RequestBody SignupRequestDto signupRequestDto) {
        userService.createUser(signupRequestDto);
        return ResponseEntity.ok("회원가입 완료");
    }
}
