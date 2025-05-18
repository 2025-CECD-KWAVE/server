package com.example.kwave.domain.news.external;

import com.example.kwave.domain.news.dto.NewsDTO;
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

        ResponseEntity<NewsListResponseWrapper> response = restTemplate.exchange(
                properties.getUrl(),
                HttpMethod.POST,
                entity,
                NewsListResponseWrapper.class
        );

        return Optional.ofNullable(response.getBody())
                .map(NewsListResponseWrapper::getReturn_object)
                .map(NewsListResponse::getDocuments)
                .orElse(Collections.emptyList());
    }

    @lombok.Data
    private static class NewsListResponseWrapper {
        private NewsListResponse return_object;
    }

    @lombok.Data
    private static class NewsListResponse {
        private List<NewsDTO> documents;
    }
}
