package com.example.kwave.domain.recommend.dto;

import com.example.kwave.domain.recommend.domain.ReactionType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NewsReactionResDto {

    private String newsId;

    private ReactionType reactionType;

}
