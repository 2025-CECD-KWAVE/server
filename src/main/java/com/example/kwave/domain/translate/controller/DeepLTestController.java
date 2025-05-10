package com.example.kwave.domain.translate.controller;

import com.example.kwave.domain.translate.dto.request.DeepLTestRequestDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/test/translation")
public class DeepLTestController {

    @Value("${deepl.api-key}")
    private String authKey;

    private final String deeplApiUrl = "https://api-free.deepl.com/v2/translate";

    @PostMapping
    public ResponseEntity<String> deepLAPITest(@RequestBody DeepLTestRequestDto translationRequestDto) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.add("Authorization", "DeepL-Auth-Key " + authKey);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("text", translationRequestDto.text());
        params.add("target_lang", translationRequestDto.targetLang());

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(params, headers);

        String response = restTemplate.postForObject(deeplApiUrl, entity, String.class);

        return ResponseEntity.ok(response);
    }
}