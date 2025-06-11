package com.example.kwave.domain.news.external;

import com.example.kwave.domain.news.dto.NewsDTO;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;




@Component
@RequiredArgsConstructor
public class NewsApiClient {

    private final RestTemplate restTemplate;
    private final NewsStoreApiProperties properties;

    public List<NewsDTO> fetchNewsListByDate(String from, String to) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> argument = new HashMap<>();
        Map<String, String> publishedAt = new HashMap<>();
        publishedAt.put("from", from);
        publishedAt.put("until", to);
        argument.put("published_at", publishedAt);

        argument.put("sort", Collections.singletonMap("date", "desc"));
        argument.put("return_from", "0");
        argument.put("return_size", "10000");

        List<String> fields = Arrays.asList(
                "news_id", "title", "content", "published_at", "enveloped_at",
                "dateline", "provider", "category", "category_incident",
                "hilight", "byline", "images", "images_caption",
                "provider_subject", "provider_news_id", "publisher_code",
                "provider_link_page", "printing_page", "tms_raw_stream"
        );
        argument.put("fields", fields);

        Map<String, Object> request = new HashMap<>();
        request.put("argument", argument);
        request.put("access_key", properties.getKey());

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
        ResponseEntity<NewsRootResponse> response = restTemplate.exchange(  //여기에 문제 발생함.
                properties.getUrl(),
                HttpMethod.POST,
                entity,
                NewsRootResponse.class
        );
        try {
            if (response.getBody() != null && response.getBody().getReturnObject() != null) {
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.registerModule(new JavaTimeModule());
                objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

                NewsListResponse parsed = objectMapper.readValue(
                        response.getBody().getReturnObject(), NewsListResponse.class);
                List<NewsDTO> documents = parsed.getDocuments();
                System.out.println("\uD83D\uDCC4 뉴스 개수: " + documents.size());
                return documents;
            } else {
                System.out.println("❌ 응답 본문이 null 또는 returnObject가 null입니다.");
                return Collections.emptyList();
            }
        } catch (Exception e) {
            System.out.println("❌ JSON 파싱 실패: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    @Data
    private static class NewsRootResponse {
        @JsonProperty("returnObject")
        private String returnObject;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class NewsListResponse {
        private List<NewsDTO> documents;
    }
}