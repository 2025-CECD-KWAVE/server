package com.example.kwave.domain.translate.domain.repository;

import com.example.kwave.domain.translate.domain.TranslatedNewsContent;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TranslatedNewsContentRepository extends CrudRepository<TranslatedNewsContent, String> {
}
