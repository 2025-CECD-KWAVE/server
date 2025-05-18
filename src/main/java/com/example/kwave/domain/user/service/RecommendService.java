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

        // 사용자 선호 및 시청 이력 바탕 카테고리별 가중치 계산
        Map<String, Integer> categoryCount = new HashMap<>();
        user.getPreferredCategories().forEach(cat -> categoryCount.merge(cat, 1, Integer::sum));
        user.getViewedCategories().forEach(cat -> categoryCount.merge(cat, 1, Integer::sum));

        // 가중치를 이용해 그룹화 <가중치, List<카테고리>>
        Map<Integer, List<String>> groupedByWeight = new HashMap<>();
        for (Map.Entry<String, Integer> entry : categoryCount.entrySet()) {
            groupedByWeight.computeIfAbsent(entry.getValue(), k -> new ArrayList<>()).add(entry.getKey());
        }

        // 가중치 내림차순으로 정렬
        List<Integer> sortedWeights = groupedByWeight.keySet().stream()
                .sorted(Comparator.reverseOrder())
                .toList();

        List<News> recommended = new ArrayList<>();
        Set<String> usedCategories = new HashSet<>();

        // 가중치 그룹별 추천 개수 설정 (상위 5개, 중위 3개, 기타 2개)
        int[] recommendCounts = {5, 3};  // 상위 1, 2등 그룹용

        // 정렬한 가중치 리스트 개수
        // ex) 가중치 5인 그룹, 4인 그룹, 3인 그룹 ...
        // 그룹의 개수와 2를 비교해 최소값을 이용해 상위 2개 그룹만 이용
        // shuffle을 통해 그룹 내 카테고리 무작위 설정
        for (int i = 0; i < Math.min(2, sortedWeights.size()); i++) {
            int weight = sortedWeights.get(i);
            List<String> categories = groupedByWeight.get(weight);
            Collections.shuffle(categories);

            recommended.addAll(
                    recommendFromCategories(categories, recommendCounts[i], usedCategories)
            );
        }

        // 하위 그룹(기타 카테고리)에서 2개 카테고리 무작위 선택, 각 1개 뉴스 추천
        List<String> remaining = new ArrayList<>(categoryCount.keySet());
        remaining.removeAll(usedCategories);
        Collections.shuffle(remaining);

        recommended.addAll(
                recommendFromCategories(remaining, 2, usedCategories)
        );

        // 중복 제거 및 최대 10개 제한
        // 뉴스 ID만 추출
        List<String> newsIds = recommended.stream()
                .map(News::getNewsId)
                .distinct()
                .limit(10)
                .toList();

        RecommendResponseDto dto = new RecommendResponseDto();
        dto.setRecommendedNewsIds(newsIds);
        return dto;
    }

     // 주어진 카테고리 리스트에서 count만큼 최신 뉴스 추천
    private List<News> recommendFromCategories(List<String> categories, int totalCount, Set<String> usedCategories) {
        List<News> result = new ArrayList<>();
        // 각 카테고리당 추천 개수
        int perCategory = Math.max(1, totalCount / Math.max(1, categories.size()));

        for (String category : categories) {
            if (result.size() >= totalCount) break;
            usedCategories.add(category);
            List<News> news = newsRepository.fetchLatestByCategory(category, PageRequest.of(0, perCategory, Sort.by("publishedAt").descending()));
            result.addAll(news);
        }
        return result;
    }
}
