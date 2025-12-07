package com.example.kwave.domain.news.domain.repository;

import com.example.kwave.domain.news.domain.News;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface NewsRepository extends JpaRepository<News, String> {

    boolean existsByNewsId(String newsId); // 중복 체크용

    @Query("SELECT n FROM News n ORDER BY n.publishedAt DESC")
    Page<News> findAllPaged(Pageable pageable);

    Optional<News> findByNewsId(String newsId);

    @Query("SELECT DISTINCT n FROM News n JOIN n.category c WHERE c IN :categories")
    List<News> fetchLatestByCategories(@Param("categories") List<String> categories, Pageable pageable);

    List<News> findBySummaryIsNullAndPublishedAtBetween(OffsetDateTime start, OffsetDateTime end);

    @Query("SELECT n FROM News n WHERE n.summary IS NULL OR n.summary = ''")
    List<News> findBySummaryIsNull();

    @Modifying
    @Query("UPDATE News n SET n.viewCount = n.viewCount + 1 WHERE n.newsId = :newsId")
    void incrementViewCount(@Param("newsId") String newsId);

    Page<News> findByNewsIdNotInOrderByEnvelopedAtDesc(List<String> newsIds, Pageable pageable);

    Page<News> findAllByOrderByEnvelopedAtDesc(Pageable pageable);

    List<News> findByNewsIdIn(List<String> newsIds);
}
