package com.example.kwave.domain.ai.domain;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

@Document(indexName = "news_vectors", createIndex = false)
@Setting(
    replicas = 2, shards = 1
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewsVectorDoc {

    @Id
    private String id;

    @Field(type = FieldType.Keyword)
    private String newsId;

    @Field(type = FieldType.Text)
    private String newsContent;

    @Field(type = FieldType.Text)
    private String newsSummary;

    private float[] embedding;
}
