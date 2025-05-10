package com.example.kwave.domain.translate.domain.repository;

import com.example.kwave.domain.translate.domain.TranslatedNewsTitle;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TranslatedNewsTitleRepository extends CrudRepository<TranslatedNewsTitle, String> {

}
