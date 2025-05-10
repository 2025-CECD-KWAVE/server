package com.example.kwave.domain.translate.domain.repository;

import com.example.kwave.domain.translate.domain.TranslatedNews;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TranslatedNewsRepository extends CrudRepository<TranslatedNews, String> {

}
