package com.example.kwave.domain.user.service;

import com.example.kwave.domain.news.domain.News;
import com.example.kwave.domain.news.domain.repository.NewsRepository;
import com.example.kwave.domain.user.domain.User;
import com.example.kwave.domain.user.domain.repository.UserRepository;
import com.example.kwave.domain.user.dto.request.RecommendRequestDto;
import com.example.kwave.domain.user.dto.response.RecommendResponseDto;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RecommendService {
    private final UserRepository userRepository;
    private final NewsRepository newsRepository;

    public RecommendService(UserRepository userRepository, NewsRepository newsRepository) {
        this.userRepository = userRepository;
        this.newsRepository = newsRepository;
    }

    public RecommendResponseDto recommendNews(RecommendRequestDto recommendRequestDto) {
        User user = userRepository.findById(recommendRequestDto.getUserId())
                .orElseThrow(() -> new RuntimeException("User Not Found"));

        // 선호/시청 카테고리 가중치 계산
        Map<String, Integer> categoryCount = new HashMap<>();
        user.getPreferredCategories().forEach(
                (category, weight) -> categoryCount.merge(category, weight, Integer::sum)
        );
        user.getViewedCategories().forEach(
                (category, weight) -> categoryCount.merge(category, weight, Integer::sum)
        );

        // 가중치별 그룹화
        Map<Integer, List<String>> groupedByWeight = new HashMap<>();
        for (Map.Entry<String, Integer> entry : categoryCount.entrySet()) {
            groupedByWeight.computeIfAbsent(entry.getValue(), k -> new ArrayList<>()).add(entry.getKey());
        }

        // 가중치 내림차순 정렬
        List<Integer> sortedWeights = groupedByWeight.keySet().stream()
                .sorted(Comparator.reverseOrder())
                .toList();

        List<News> recommended = new ArrayList<>();
        Set<String> usedNewsIds = new HashSet<>();

        int[] recommendCounts = {5, 3};

        // 상위 그룹 추천
        for (int i = 0; i < Math.min(2, sortedWeights.size()); i++) {
            int weight = sortedWeights.get(i);
            List<String> categories = groupedByWeight.get(weight);

            Collections.shuffle(categories);
            if (categories.size() > 20) {
                categories = categories.subList(0, 20);
            }

            recommendFromCategories(categories, recommendCounts[i], usedNewsIds, recommended);
        }

        // 하위 그룹 추천
        List<String> remaining = new ArrayList<>();
        for (int i = 2; i < sortedWeights.size(); i++) {
            remaining.addAll(groupedByWeight.get(sortedWeights.get(i)));
        }
        Collections.shuffle(remaining);

        if (remaining.size() > 20) {
            remaining = remaining.subList(0, 20);
        }

        recommendFromCategories(remaining, 2, usedNewsIds, recommended);

        // 최대 10개까지만 추출
        List<String> newsIds = recommended.stream()
                .map(News::getNewsId)
                .limit(10)
                .toList();

        RecommendResponseDto dto = new RecommendResponseDto();
        dto.setRecommendedNewsIds(newsIds);
        return dto;
    }

    private void recommendFromCategories(List<String> categories, int totalCount,
                                         Set<String> usedNewsIds, List<News> result) {

        List<News> candidates = newsRepository.fetchLatestByCategories(
                categories, PageRequest.of(0, 20, Sort.by("publishedAt").descending()));

        Collections.shuffle(candidates); // 편향 완화용 셔플

        int added = 0;
        for (News news : candidates) {
            if (added >= totalCount) break;
            if (!usedNewsIds.contains(news.getNewsId())) {
                result.add(news);
                usedNewsIds.add(news.getNewsId());
                added++;
            }
        }
    }
}
