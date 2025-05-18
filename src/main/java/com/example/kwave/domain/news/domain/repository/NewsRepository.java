package com.example.kwave.domain.news.domain.repository;

import com.example.kwave.domain.news.domain.News;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface NewsRepository extends JpaRepository<News, String> {
    @Query("select n from News n WHERE n.category = :category")
    List<News> fetchLatestByCategory(String category, Pageable pageable);

}
