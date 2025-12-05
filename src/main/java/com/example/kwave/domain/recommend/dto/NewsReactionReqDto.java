package com.example.kwave.domain.recommend.dto;

import com.example.kwave.domain.recommend.domain.ReactionType;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NewsReactionReqDto {

    private UUID userId;

    private String newsId;

    private ReactionType reactionType;
}
