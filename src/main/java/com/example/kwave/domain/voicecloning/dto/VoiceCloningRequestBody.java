package com.example.kwave.domain.voicecloning.dto;

// API 요청 본문은 (text, model_id) 두 필드를 가집니다.
public record VoiceCloningRequestBody(
    String text,
    String model_id
) {}