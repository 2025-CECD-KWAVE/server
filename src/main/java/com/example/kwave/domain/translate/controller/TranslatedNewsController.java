package com.example.kwave.domain.translate.controller;

import com.example.kwave.domain.translate.domain.TranslatedNews;
import com.example.kwave.domain.translate.service.TranslatedNewsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

@Controller("/api/translatedNews")
@RequiredArgsConstructor
public class TranslatedNewsController {

    private final TranslatedNewsService translatedNewsService;

}
