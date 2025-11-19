package com.example.kwave.domain.news.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "news_embedding")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewsEmbedding {

    // News의 PK와 동일하게 사용
    @Id
    @Column(name = "news_id", nullable = false, unique = true)
    private String newsId;

    // 1536차원의 벡터를 byte[]로 직렬화 하여 저장
    @Lob
    @Column(name = "embedding", columnDefinition = "BLOB", nullable = false)
    private byte[] embedding;
}
