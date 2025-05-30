package com.example.kwave.domain.user.dto.request;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Data
@Getter
@Setter
public class RecommendRequestDto {
    private UUID userId; //사용자 식별
}
