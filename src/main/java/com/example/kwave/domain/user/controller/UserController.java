package com.example.kwave.domain.user.controller;

import com.example.kwave.domain.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/user")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/{userId}/view")
    public ResponseEntity<String> updateView(@PathVariable UUID userId,
                                             @RequestBody List<String> categories) {
        userService.updateViewedCategories(userId, categories);
        return ResponseEntity.ok("시청 이력 업데이트");
    }
}
