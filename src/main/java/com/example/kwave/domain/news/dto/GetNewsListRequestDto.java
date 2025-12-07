package com.example.kwave.domain.news.dto;

import java.util.List;

public record GetNewsListRequestDto(
        List<String> ids
) {}