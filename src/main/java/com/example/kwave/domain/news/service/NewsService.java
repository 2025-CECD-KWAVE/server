package com.example.kwave.domain.news.service;

import com.example.kwave.domain.news.domain.News;
import com.example.kwave.domain.news.domain.repository.NewsRepository;
import com.example.kwave.domain.news.dto.NewsDTO;
import com.example.kwave.domain.news.external.NewsApiClient;
import com.example.kwave.domain.user.domain.User;
import com.example.kwave.domain.user.service.UserService;
import com.example.kwave.domain.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NewsService {

    private final NewsApiClient newsApiClient;
    private final NewsRepository newsRepository;
    private final UserService userService;
    private final UserRepository userRepository;

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

    public void userWatched(UUID userId, String newsId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        News news = newsRepository.findById(newsId)
                .orElseThrow(() -> new RuntimeException("News not found"));

        List<String> categories = news.getCategory();

        userService.updateViewedCategories(userId, categories);
    }
}
