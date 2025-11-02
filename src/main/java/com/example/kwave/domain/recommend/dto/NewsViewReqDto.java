package com.example.kwave.domain.recommend.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class NewsViewReqDto {

    private UUID userId;

    private String newsId;
}
