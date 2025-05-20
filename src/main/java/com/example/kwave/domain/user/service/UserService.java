package com.example.kwave.domain.user.service;

import com.example.kwave.domain.user.domain.User;
import com.example.kwave.domain.user.domain.repository.UserRepository;
import com.example.kwave.domain.user.dto.request.SignupRequestDto;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User createUser(SignupRequestDto signupRequestDto) {
        // 회원가입 시 선호하는 카테고리에 가중치 3 부여
        Map<String, Integer> userPreferredCategory = new HashMap<>();
        for (String category : signupRequestDto.getPreferredCategories()) {
            userPreferredCategory.put(category, 3);
        }

        User user = new User();
        user.setUsername(signupRequestDto.getUsername());
        user.setEmail(signupRequestDto.getEmail()); // 회원가입 시 중복 X 처리 필요
        user.setPassword(signupRequestDto.getPassword()); // 암호화 기능 필요
        user.setNationality(signupRequestDto.getNationality());
        user.setLanguage(signupRequestDto.getLanguage());
        user.setPreferredCategories(userPreferredCategory);
        user.setViewedCategories(new HashMap<>()); // 시청 이력 초기화
        
        this.userRepository.save(user);
        return user;
    }

    public void updateViewedCategories(UUID userId, List<String> categories) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Map<String, Integer> viewedMap = user.getViewedCategories();

        for (String category : categories) {
            viewedMap.put(category, viewedMap.getOrDefault(category, 0) + 1);
        }

        userRepository.save(user);
    }

}
