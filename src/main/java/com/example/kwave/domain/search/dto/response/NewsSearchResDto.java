package com.example.kwave.domain.search.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class NewsSearchResDto {

    private List<SearchingResultDto> results;

    private Long totalHits;

    private Integer currentPage;

    private Integer pageSize;
}
