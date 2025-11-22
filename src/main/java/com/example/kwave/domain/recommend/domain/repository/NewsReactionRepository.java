package com.example.kwave.domain.recommend.domain.repository;

import com.example.kwave.domain.recommend.domain.NewsReaction;
import com.example.kwave.domain.recommend.domain.ReactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NewsReactionRepository extends JpaRepository<NewsReaction, Long> {

    // 특정 사용자의 특정 뉴스에 대한 반응 조회
    Optional<NewsReaction> findByUserIdAndNewsId(UUID userId, String newsId);

    // 이미 반응이 있는지 확인
    boolean existsByUserIdAndNewsId(UUID userId, String newsId);

    // 특정 사용자의 모든 반응 조회
    List<NewsReaction> findByUserId(UUID userId);

    List<NewsReaction> findByReactionTypeIn(List<ReactionType> types);

    List<NewsReaction> findByUserIdAndReactionTypeIn(UUID userId, List<ReactionType> types);

    List<NewsReaction> findByUserIdAndReactionType(UUID userId, ReactionType type);
}
