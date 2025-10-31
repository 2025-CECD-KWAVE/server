package com.example.kwave.domain.search.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NewsSearchReqDto {

    private String query;

    private Integer page = 0;

    private Integer pageSize = 10;
}
