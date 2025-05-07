package com.example.kwave.domain.news.domain;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "news")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class News {

    @Id
    @Column(name = "news_id", nullable = false, unique = true)
    private String newsId;

    private String title;

    @Lob
    private String content;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "enveloped_at")
    private LocalDateTime envelopedAt;

    @Column(name = "dateline")
    private LocalDateTime dateline;

    private String provider;

    private String byline;

    @Column(name = "provider_link_page")
    private String providerLinkPage;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "news_category", joinColumns = @JoinColumn(name = "news_id"))
    @Column(name = "category")
    private List<String> category;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "news_incident_category", joinColumns = @JoinColumn(name = "news_id"))
    @Column(name = "incident_category")
    private List<String> categoryIncident;
}