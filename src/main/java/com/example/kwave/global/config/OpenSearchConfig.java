package com.example.kwave.global.config;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.erhlc.AbstractElasticsearchConfiguration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@Configuration
@EnableElasticsearchRepositories(basePackages = "com.example.kwave.domain.ai.domain.repository")
public class OpenSearchConfig extends AbstractElasticsearchConfiguration {

    @Value("${spring.opensearch.uris}")
    private String openSearchUri;

    @Value("${spring.opensearch.username}")
    private String username;

    @Value("${spring.opensearch.password}")
    private String password;

    @Override
    @Bean
    public RestHighLevelClient elasticsearchClient() {

        // URI 파 (https:// 제거)
        String host = openSearchUri.replace("https://", "").replace("http://", "");
        boolean isHttps = openSearchUri.startsWith("https");
        int port = isHttps ? 443 : 9200;

        // AWS Opensearch 인증 설정
        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(
                AuthScope.ANY,
                new UsernamePasswordCredentials(username, password)
        );

        return new RestHighLevelClient(
                RestClient.builder(new HttpHost(host, port, isHttps ? "https" : "http"))
                        .setHttpClientConfigCallback(httpClientBuilder
                                -> httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider))
        );
    }
}
