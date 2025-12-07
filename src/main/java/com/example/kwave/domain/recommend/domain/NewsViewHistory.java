package com.example.kwave.domain.recommend.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "news_view_history",
        indexes = {
                @Index(name = "idx_user_id", columnList = "user_id"),
                @Index(name = "idx_news_id", columnList = "news_id"),
                @Index(name = "idx_viewed_at", columnList = "viewed_at"),
                @Index(name = "idx_user_viewed", columnList = "user_id, viewed_at")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewsViewHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "news_id")
    private String newsId;

    @Column(name = "viewed_at")
    private OffsetDateTime viewedAt;
}
