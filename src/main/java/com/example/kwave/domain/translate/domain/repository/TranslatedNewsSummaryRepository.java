package com.example.kwave.domain.translate.domain.repository;

import com.example.kwave.domain.translate.domain.TranslatedNewsSummary;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TranslatedNewsSummaryRepository extends CrudRepository<TranslatedNewsSummary, String> {

}
