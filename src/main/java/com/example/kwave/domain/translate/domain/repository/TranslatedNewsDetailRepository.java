package com.example.kwave.domain.translate.domain.repository;

import com.example.kwave.domain.translate.domain.TranslatedNewsDetail;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TranslatedNewsDetailRepository extends CrudRepository<TranslatedNewsDetail, String> {
}
