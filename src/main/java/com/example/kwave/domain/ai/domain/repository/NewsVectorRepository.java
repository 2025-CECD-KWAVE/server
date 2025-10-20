package com.example.kwave.domain.ai.domain.repository;

import com.example.kwave.domain.ai.domain.NewsVectorDoc;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NewsVectorRepository extends ElasticsearchRepository<NewsVectorDoc, String> {

    boolean existsByNewsId(String newsId);

    void deleteByNewsId(String newsId);
}
