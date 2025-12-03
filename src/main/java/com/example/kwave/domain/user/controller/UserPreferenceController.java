package com.example.kwave.domain.user.controller;

import com.example.kwave.domain.user.service.UserPreferenceBatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users/preferences")
public class UserPreferenceController {

    private final UserPreferenceBatchService userPreferenceBatchService;

    /**
     * 전체 유저 선호 벡터 배치 실행 (테스트)
     */
    @PostMapping("/rebuild-all")
    public ResponseEntity<Void> rebuildAll() {
        userPreferenceBatchService.rebuildAllUserPreferenceVectors();
        return ResponseEntity.ok().build();
    }

    /**
     * 특정 userId를 직접 지정 재계 (관리자용)
     */
    @PostMapping("/rebuild/{userId}")
    public ResponseEntity<Void> rebuildForUser(@PathVariable UUID userId) {
        userPreferenceBatchService.rebuildUserPreferenceVector(userId);
        return ResponseEntity.ok().build();
    }

}
