package com.example.kwave.domain.recommend.domain.repository;

import com.example.kwave.domain.recommend.domain.NewsViewHistory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NewsViewHistoryRepository extends JpaRepository<NewsViewHistory, Integer> {

    // 사용자의 최근 뉴스 시청 이력 조회
    List<NewsViewHistory> findByUserIdOrderByViewedAtDesc(UUID userId,  Pageable pageable);

    // 특정 뉴스를 보았는지 확인
    boolean existsByUserIdAndNewsId(UUID userId, String newsId);
}
