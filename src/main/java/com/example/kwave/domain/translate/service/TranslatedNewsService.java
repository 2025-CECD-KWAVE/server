package com.example.kwave.domain.translate.service;

import com.example.kwave.domain.news.domain.News;
import com.example.kwave.domain.news.domain.repository.NewsRepository;
import com.example.kwave.domain.translate.domain.TargetLangCode;
import com.example.kwave.domain.translate.domain.TranslatedNews;
import com.example.kwave.domain.translate.domain.repository.TranslatedNewsRepository;
import com.example.kwave.domain.translate.dto.response.TranslateNewsResponseDto;
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

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TranslatedNewsService {

    @Value("${deepl.api-key}")
    private String authKey;

    private final String deeplApiUrl = "https://api-free.deepl.com/v2/translate";

    private final TranslatedNewsRepository translatedNewsRepository;
    private final NewsRepository newsRepository;

    public TranslatedNews getOrTranslateNews(String newsId, TargetLangCode targetLangCode) {

        String redisKey = newsId + ":" + targetLangCode;
        Optional<TranslatedNews> cachedTranslatedNews = translatedNewsRepository.findById(redisKey);
        if (cachedTranslatedNews.isPresent()) {
            return cachedTranslatedNews.get();
        }

        News news = newsRepository.findById(newsId)
                .orElseThrow(()-> new NotFoundException("해당 뉴스가 없습니다"));

        TranslateNewsResponseDto translateNewsResponseDto = translateNews(news.getTitle(), news.getContent(), targetLangCode);

        TranslatedNews translatedNews = TranslatedNews.builder()
                .translatedNewsId(newsId)
                .translatedTitle(translateNewsResponseDto.getTranslations().get(0).getText())
                .translatedContent(translateNewsResponseDto.getTranslations().get(1).getText())
                .build();

        return translatedNews;

    }

    public TranslateNewsResponseDto translateNews(String title, String content, TargetLangCode targetLangCode) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.add("Authorization", "DeepL-Auth-Key " + authKey);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("text", title);    // 타이틀
        params.add("text", content);  // 내용
        params.add("target_lang", targetLangCode.toString());


        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(params, headers);

        return  restTemplate.postForObject(deeplApiUrl, entity, TranslateNewsResponseDto.class);
    }
}
