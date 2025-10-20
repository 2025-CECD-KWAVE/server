package com.example.kwave.domain.ai.domain;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Document(indexName = "news_vector")
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

    @Field(type = FieldType.Dense_Vector, dims = 1536)
    private float[] embedding;
}
