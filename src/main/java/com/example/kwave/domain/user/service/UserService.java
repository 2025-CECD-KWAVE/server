package com.example.kwave.domain.user.service;

import com.example.kwave.domain.user.domain.User;
import com.example.kwave.domain.user.domain.repository.UserRepository;
import com.example.kwave.domain.user.dto.request.ClickLogRequestDto;
import com.example.kwave.domain.user.dto.request.PreferVectorReqDto;
import com.example.kwave.domain.user.dto.request.SignupRequestDto;
import com.example.kwave.domain.user.dto.response.PreferVectorResDto;
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
        // 회원가입 시 선호하는 카테고리에 가중치 3 부여
        Map<String, Integer> userPreferredCategory = new HashMap<>();
        for (String category : signupRequestDto.getPreferredCategories()) {
            userPreferredCategory.put(category, 3);
        }

        User user = new User();
        user.setUserId(UUID.randomUUID());
        user.setUsername(signupRequestDto.getUsername()); // 회원가입 시 중복 처리 필요
        user.setNickname(signupRequestDto.getNickname()); // 회원가입 시 중복 처리 필요
        user.setPassword(bCryptPasswordEncoder.encode(signupRequestDto.getPassword()));
        user.setNationality(signupRequestDto.getNationality());
        user.setLanguage(signupRequestDto.getLanguage());
        user.setPreferredCategories(userPreferredCategory);
        user.setViewedCategories(new HashMap<>()); // 시청 이력 초기화

        // 초기 선호 벡터 저장
        float[] initialVector = new float[1536];
        user.setPreferenceVector(FloatArrayConverter.toBytes(initialVector));
        
        this.userRepository.save(user);
        return user;
    }

    // 벡터 저장
    public void saveUserVector(PreferVectorReqDto reqDto) {
        User user = userRepository.findById(reqDto.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // float[] → byte[] 변환 후 저장
        user.setPreferenceVector(FloatArrayConverter.toBytes(reqDto.getVector()));
        userRepository.save(user);
    }

    // 벡터 불러오기 (추천할 때, Redis miss 시)
    public PreferVectorResDto getUserVector(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        float[] vector = FloatArrayConverter.toFloatArray(user.getPreferenceVector());

        return PreferVectorResDto.builder()
                .userId(userId)
                .vector(vector)
                .build();
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
