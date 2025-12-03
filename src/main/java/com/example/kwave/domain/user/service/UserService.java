package com.example.kwave.domain.user.service;

import com.example.kwave.domain.user.domain.User;
import com.example.kwave.domain.user.domain.repository.UserRepository;
import com.example.kwave.domain.user.dto.request.ClickLogRequestDto;
import com.example.kwave.domain.user.dto.request.SignupRequestDto;
import com.example.kwave.global.util.FloatArrayConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public User createUser(SignupRequestDto signupRequestDto) {

        User user = new User();
        user.setUserId(UUID.randomUUID());
        user.setUsername(signupRequestDto.getUsername()); // 회원가입 시 중복 처리 필요
        user.setNickname(signupRequestDto.getNickname()); // 회원가입 시 중복 처리 필요
        user.setPassword(bCryptPasswordEncoder.encode(signupRequestDto.getPassword()));
        user.setNationality(signupRequestDto.getNationality());
        user.setLanguage(signupRequestDto.getLanguage());

        // 초기 선호 벡터 저장 -> null
        
        this.userRepository.save(user);
        return user;
    }

    // 벡터 불러오기 (추천할 때, Redis miss 시)
    public float[] loadUserPreferenceVector(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        byte[] prefVector = user.getPreferenceVector();

        if (prefVector == null) {
            return null;
        }

        return FloatArrayConverter.toFloatArray(prefVector);
    }

    public void updateClickLog (UUID userId, ClickLogRequestDto clickLogRequestDto){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Map<Boolean, Integer> clickLogSummary = user.getClickSummary();

        if(clickLogSummary == null){
            clickLogSummary = new HashMap<>();
        }

        boolean isFromRecommend = clickLogRequestDto.isFromRecommend();

        clickLogSummary.put(isFromRecommend, clickLogSummary.getOrDefault(isFromRecommend, 0) + 1);

        user.setClickSummary(clickLogSummary);
        userRepository.save(user);
    }
}
