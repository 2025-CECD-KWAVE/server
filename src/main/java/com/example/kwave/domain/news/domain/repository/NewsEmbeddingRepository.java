package com.example.kwave.domain.news.domain.repository;

import com.example.kwave.domain.news.domain.NewsEmbedding;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NewsEmbeddingRepository extends JpaRepository<NewsEmbedding, String> {
}
