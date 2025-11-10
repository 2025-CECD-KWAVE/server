package com.example.kwave.domain.recommend.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReactionType {
    Like("좋아요", 10),
    Dislike("싫어요", -20),
    JustWatch("시청", 3);

    private final String description;
    private final int weight;

}
