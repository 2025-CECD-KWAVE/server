package com.example.kwave.domain.news.domain.repository;

import com.example.kwave.domain.news.domain.News;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NewsRepository extends JpaRepository<News, String> {

    @Query("SELECT n FROM News n JOIN n.category c WHERE c = :category ORDER BY n.publishedAt DESC")
    List<News> fetchLatestByCategory(String category, Pageable pageable);

}
