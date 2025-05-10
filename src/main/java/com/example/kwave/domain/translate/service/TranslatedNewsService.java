package com.example.kwave.domain.translate.service;

import com.example.kwave.domain.news.domain.News;
import com.example.kwave.domain.news.domain.repository.NewsRepository;
import com.example.kwave.domain.translate.domain.TargetLangCode;
import com.example.kwave.domain.translate.domain.TranslatedNewsContent;
import com.example.kwave.domain.translate.domain.TranslatedNewsTitle;
import com.example.kwave.domain.translate.domain.repository.TranslatedNewsContentRepository;
import com.example.kwave.domain.translate.domain.repository.TranslatedNewsTitleRepository;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TranslatedNewsService {

    @Value("${deepl.api-key}")
    private String authKey;

    private final String deeplApiUrl = "https://api-free.deepl.com/v2/translate";

    private final TranslatedNewsTitleRepository translatedNewsTitleRepository;
    private final TranslatedNewsContentRepository translatedNewsContentRepository;
    private final NewsRepository newsRepository;

    public TranslatedNewsTitle getOrTranslateTitle(String newsId, TargetLangCode targetLangCode) {

        String redisKey = newsId + ":" + targetLangCode + ":Title"; // newsId:targetLang:Title을 key로 검색
        Optional<TranslatedNewsTitle> cachedTranslatedNewsTitle = translatedNewsTitleRepository.findById(redisKey);
        if (cachedTranslatedNewsTitle.isPresent()) {
            return cachedTranslatedNewsTitle.get();
        }

        News news = newsRepository.findById(newsId)
                .orElseThrow(()-> new NotFoundException("해당 뉴스가 없습니다"));

        List<String> title = new ArrayList<>();

        title.add(news.getTitle()); // 목록 조회에서 추가로 보여줄 정보에 따라서, List에 담아서 확장 가능

        TranslateResponseDto translateResponseDto = translate(title, targetLangCode);

        TranslatedNewsTitle translatedNewsTitle = TranslatedNewsTitle.builder()
                .redisKey(redisKey)
                .translatedTitle(translateResponseDto.getTranslations().get(0).getText())
                .build();

        translatedNewsTitleRepository.save(translatedNewsTitle);

        return translatedNewsTitle;

    }

    public TranslatedNewsContent getOrTranslateContent(String newsId, TargetLangCode targetLangCode) {

        String redisKey = newsId + ":" + targetLangCode + ":Content"; // newsId:targetLang:Content을 key로 검색
        Optional<TranslatedNewsContent> cachedTranslatedNewsContent = translatedNewsContentRepository.findById(redisKey);
        if (cachedTranslatedNewsContent.isPresent()) {
            return cachedTranslatedNewsContent.get();
        }

        News news = newsRepository.findById(newsId)
                .orElseThrow(()-> new NotFoundException("해당 뉴스가 없습니다"));

        List<String> content = new ArrayList<>();

        content.add(news.getContent());

        TranslateResponseDto translateResponseDto = translate(content, targetLangCode);

        TranslatedNewsContent translatedNewsContent = TranslatedNewsContent.builder()
                .redisKey(redisKey)
                .translatedContent(translateResponseDto.getTranslations().get(0).getText())
                .build();

        translatedNewsContentRepository.save(translatedNewsContent);

        return translatedNewsContent;
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
        params.add("target_lang", targetLangCode.toString());

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(params, headers);

        return  restTemplate.postForObject(deeplApiUrl, entity, TranslateResponseDto.class);
    }
}
