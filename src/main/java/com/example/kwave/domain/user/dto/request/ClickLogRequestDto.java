package com.example.kwave.domain.user.dto.request;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class ClickLogRequestDto {
    private boolean fromRecommend;
}
