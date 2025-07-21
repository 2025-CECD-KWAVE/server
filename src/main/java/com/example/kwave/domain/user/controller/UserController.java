package com.example.kwave.domain.user.controller;

import com.example.kwave.domain.user.domain.CustomUserDetails;
import com.example.kwave.domain.user.dto.request.ClickLogRequestDto;
import com.example.kwave.domain.user.dto.request.SignupRequestDto;
import com.example.kwave.domain.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
@Tag(name = "User", description = "유저 관련 API")
public class UserController {
    private final UserService userService;

    @PostMapping("/signup")
    @Operation(summary = "유저 회원가입", description = "유저 회원가입 처리")
    public ResponseEntity<String> signUpUser (@RequestBody SignupRequestDto signupRequestDto) {
        userService.createUser(signupRequestDto);
        return ResponseEntity.ok("회원가입 완료");
    }

    //recommend 된 뉴스 열람했는지 확인하는 컨트롤러
    @PostMapping("/click-log")
    @Operation(summary = "유저 뉴스 조회 로그 수집", description = "조회한 뉴스가 추천된 뉴스인지 T/F 카운트")
    public ResponseEntity<String> clickLog (@AuthenticationPrincipal CustomUserDetails user, @RequestBody ClickLogRequestDto clickLogRequestDto) {
        UUID userId = user.getId();
        userService.updateClickLog(userId, clickLogRequestDto);
        return ResponseEntity.ok("클릭 로그 저장 완료");
    }
}
