package com.example.kwave.domain.news.domain.repository;

import com.example.kwave.domain.news.domain.News;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NewsRepository extends JpaRepository<News, String> {
    boolean existsByNewsId(String newsId); // 중복 체크용
}
