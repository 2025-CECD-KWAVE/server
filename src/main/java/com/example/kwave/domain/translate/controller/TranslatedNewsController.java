package com.example.kwave.domain.translate.controller;

import com.example.kwave.domain.translate.service.TranslatedNewsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;

@Controller("/api/translated")
@RequiredArgsConstructor
public class TranslatedNewsController {

    private final TranslatedNewsService translatedNewsService;

}
