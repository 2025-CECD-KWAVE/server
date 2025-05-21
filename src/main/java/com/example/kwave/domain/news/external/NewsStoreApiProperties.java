package com.example.kwave.domain.news.external;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "newstore.api")
public class NewsStoreApiProperties {
    private String key;
    private String url;
}