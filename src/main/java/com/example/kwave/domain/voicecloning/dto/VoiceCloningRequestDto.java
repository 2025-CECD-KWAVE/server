package com.example.kwave.domain.voicecloning.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor // JSON 데이터를 객체로 변환하기 위해 기본 생성자가 필요합니다.
public class VoiceCloningRequestDto {

    private String voiceId; 
    private String text;    
}