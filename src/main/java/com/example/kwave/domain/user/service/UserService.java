package com.example.kwave.domain.user.service;

import com.example.kwave.domain.user.domain.User;
import com.example.kwave.domain.user.domain.repository.UserRepository;
import com.example.kwave.domain.user.dto.request.SignupRequestDto;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User createUser(SignupRequestDto signupRequestDto) {
        User user = new User();
        user.setUsername(signupRequestDto.getUsername());
        user.setEmail(signupRequestDto.getEmail()); // 회원가입 시 중복 X 처리 필요
        user.setPassword(signupRequestDto.getPassword()); // 암호화 기능 필요
        user.setNationality(signupRequestDto.getNationality());
        user.setLanguage(signupRequestDto.getLanguage());
        user.setPreferredCategories(signupRequestDto.getPreferredCategories());

        this.userRepository.save(user);
        return user;
    }


}
