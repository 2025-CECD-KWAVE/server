package com.example.kwave.domain.news.service;

import com.example.kwave.domain.news.domain.News;
import com.example.kwave.domain.news.domain.repository.NewsRepository;
import com.example.kwave.domain.news.dto.NewsDTO;
import com.example.kwave.domain.news.dto.NewsDetailDTO;
import com.example.kwave.domain.news.dto.NewsSummaryDTO;
import com.example.kwave.domain.news.external.NewsApiClient;
import com.example.kwave.global.util.TimeUtils;
import com.example.kwave.domain.user.domain.User;
import com.example.kwave.domain.user.service.UserService;
import com.example.kwave.domain.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;
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
                if (!dto.isCultureGeneralOrEntertainmentNews()) {
                    continue; // 문화>문화일반, 문화>방송_연예가 아니면 저장x
                }
                newsRepository.save(dto.toEntity());
            } else {
                System.out.println("이미 존재하는 뉴스: " + dto.getNewsId());
            }
        }
    }

    public List<NewsSummaryDTO> getNewsSummaries(Pageable pageable) {
        Page<News> page = newsRepository.findAll(pageable);
        return page.getContent().stream().map(news -> {
            String summary = news.getContent() != null && news.getContent().length() > 100
                    ? news.getContent().substring(0, 100) + "..."
                    : news.getContent();

            String timeAgo = TimeUtils.getTimeAgo(
                    news.getPublishedAt().atZoneSameInstant(ZoneId.of("Asia/Seoul")).toLocalDateTime()
            );

            String thumbnailUrl = (news.getImageUrls() != null && !news.getImageUrls().isEmpty())
                    ? news.getImageUrls().get(0)
                    : null;

            return NewsSummaryDTO.builder()
                    .newsId(news.getNewsId())
                    .title(news.getTitle())
                    .summary(summary)
                    .timeAgo(timeAgo)
                    .thumbnailUrl(thumbnailUrl) // ✅
                    .build();
        }).collect(Collectors.toList());
    }

    public NewsDetailDTO getNewsDetail(String newsId) {
        News news = newsRepository.findById(newsId)
                .orElseThrow(() -> new RuntimeException("뉴스를 찾을 수 없습니다."));
        return NewsDetailDTO.builder()
                .newsId(news.getNewsId())
                .title(news.getTitle())
                .content(news.getContent())
                .provider(news.getProvider())
                .byline(news.getByline())
                .publishedAt(news.getPublishedAt().toLocalDateTime())
                .providerLinkPage(news.getProviderLinkPage())
                .imageUrls(news.getImageUrls())
                .build();
    }

}
