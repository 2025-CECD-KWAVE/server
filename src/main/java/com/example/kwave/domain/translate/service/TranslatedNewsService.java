package com.example.kwave.domain.translate.service;

import com.example.kwave.domain.news.domain.News;
import com.example.kwave.domain.news.domain.repository.NewsRepository;
import com.example.kwave.domain.news.dto.NewsDetailDTO;
import com.example.kwave.domain.news.dto.NewsSummaryDTO;
import com.example.kwave.domain.translate.domain.TargetLangCode;
import com.example.kwave.domain.translate.domain.TranslatedNewsDetail;
import com.example.kwave.domain.translate.domain.TranslatedNewsSummary;
import com.example.kwave.domain.translate.domain.repository.TranslatedNewsDetailRepository;
import com.example.kwave.domain.translate.domain.repository.TranslatedNewsSummaryRepository;
import com.example.kwave.domain.translate.dto.response.TranslateResponseDto;
import com.example.kwave.global.error.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TranslatedNewsService {

    @Value("${deepl.api-key}")
    private String authKey;

    private final String deeplApiUrl = "https://api-free.deepl.com/v2/translate";

    private final TranslatedNewsSummaryRepository translatedNewsSummaryRepository;
    private final TranslatedNewsDetailRepository translatedNewsDetailRepository;
    private final NewsRepository newsRepository;

    public List<NewsSummaryDTO> getOrTranslateSummary(List<NewsSummaryDTO> newsSummaryDTOList, TargetLangCode targetLangCode) {

        List<NewsSummaryDTO> translatedNewsSummaryList = new ArrayList<>();
        for(NewsSummaryDTO newsSummaryDTO : newsSummaryDTOList) {
            String newsId = newsSummaryDTO.getNewsId();
            String redisKey = newsId + ":" + targetLangCode + ":Summary"; // newsId:targetLang:Summary을 key로 검색
            Optional<TranslatedNewsSummary> cachedTranslatedNewsSummary = translatedNewsSummaryRepository.findById(redisKey);
            if (cachedTranslatedNewsSummary.isPresent()) {
                translatedNewsSummaryList.add(cachedTranslatedNewsSummary.get().toNewsSummaryDto());
                continue;
            }

            List<String> summary = new ArrayList<>();

            summary.add(newsSummaryDTO.getTitle()); // 목록 조회에서 추가로 보여줄 정보에 따라서, List에 담아서 확장 가능
            summary.add(newsSummaryDTO.getSummary());
            summary.add(newsSummaryDTO.getTimeAgo());

            TranslateResponseDto translateResponseDto = translate(summary, targetLangCode);

            TranslatedNewsSummary translatedNewsSummary = TranslatedNewsSummary.builder()
                    .redisKey(redisKey)
                    .translatedTitle(translateResponseDto.getTranslations().get(0).getText())
                    .translatedSummary(translateResponseDto.getTranslations().get(1).getText())
                    .timeAgo(translateResponseDto.getTranslations().get(2).getText())
                    .build();

            translatedNewsSummaryRepository.save(translatedNewsSummary);
            translatedNewsSummaryList.add(translatedNewsSummary.toNewsSummaryDto());
        }

        return translatedNewsSummaryList;

    }

    public NewsDetailDTO getOrTranslateDetail(NewsDetailDTO newsDetailDTO, TargetLangCode targetLangCode) {

        String newsId = newsDetailDTO.getNewsId();
        String redisKey = newsId + ":" + targetLangCode + ":Detail"; // newsId:targetLang:Detail을 key로 검색
        Optional<TranslatedNewsDetail> cachedTranslatedNewsContent = translatedNewsDetailRepository.findById(redisKey);
        if (cachedTranslatedNewsContent.isPresent()) {
            return cachedTranslatedNewsContent.get().toNewsDetailDto();
        }

        List<String> detail = new ArrayList<>();

        detail.add(newsDetailDTO.getTitle());
        detail.add(newsDetailDTO.getContent());
        detail.add(newsDetailDTO.getProvider());
        detail.add(newsDetailDTO.getByline());


        TranslateResponseDto translateResponseDto = translate(detail, targetLangCode);

        TranslatedNewsDetail translatedNewsDetail = TranslatedNewsDetail.builder()
                .redisKey(redisKey)
                .translatedTitle(translateResponseDto.getTranslations().get(0).getText())
                .translatedContent(translateResponseDto.getTranslations().get(1).getText())
                .translatedProvider(translateResponseDto.getTranslations().get(2).getText())
                .translatedByline(translateResponseDto.getTranslations().get(3).getText())
                .publishedAt(newsDetailDTO.getPublishedAt())
                .providerLinkPage(newsDetailDTO.getProviderLinkPage())
                .build();

        translatedNewsDetailRepository.save(translatedNewsDetail);

        return translatedNewsDetail.toNewsDetailDto();
    }

    public TranslateResponseDto translate(List<String> translateLines, TargetLangCode targetLangCode) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.add("Authorization", "DeepL-Auth-Key " + authKey);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        for (String translateLine : translateLines ) { // 확장성을 고려해, List로 받음
            params.add("text", translateLine);
        }
        params.add("target_lang", targetLangCode.getStringCode());

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(params, headers);

        return  restTemplate.postForObject(deeplApiUrl, entity, TranslateResponseDto.class);
    }

    public TargetLangCode convertLocaleToTargetLangCode (Locale locale) {
        return TargetLangCode.convertLocale(locale)
                .orElse(TargetLangCode.KO); // 없으면 한국어로
    }
}
