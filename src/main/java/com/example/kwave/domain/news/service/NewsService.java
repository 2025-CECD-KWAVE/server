package com.example.kwave.domain.news.service;

import com.example.kwave.domain.news.domain.News;
import com.example.kwave.domain.news.domain.repository.NewsRepository;
import com.example.kwave.domain.news.dto.NewsDTO;
import com.example.kwave.domain.news.external.NewsApiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NewsService {

    private final NewsApiClient newsApiClient;
    private final NewsRepository newsRepository;

    public void saveNewsIfNotExists(News news) {
        if (!newsRepository.existsByNewsId(news.getNewsId())) {
            newsRepository.save(news);
        }
    }
    public void fetchAndSaveAll(String from, String to) {
        List<NewsDTO> newsList = newsApiClient.fetchNewsListByDate(from, to);

        for (NewsDTO dto : newsList) {
            System.out.println("받은 뉴스 ID: " + dto.getNewsId());
            if (!newsRepository.existsById(dto.getNewsId())) {
                newsRepository.save(dto.toEntity());
            } else {
                System.out.println("이미 존재하는 뉴스: " + dto.getNewsId());
            }
        }
    }
}
