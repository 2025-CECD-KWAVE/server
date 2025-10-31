package com.example.kwave.domain.news.domain;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

@Entity
@Table(name = "news")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class News {


    //API의 "news_id에 해당, 뉴스의 고유식별자 id임
    @Id
    @Column(name = "news_id", nullable = false, unique = true)
    private String newsId;


    // API의 "title"에 해당
    private String title;

    // API의 "content"에 해당됨, 본문 내용이 들어감. 긴 문자열의 형태임
    @Lob
    private String content;

    // API의 "published_at", 뉴스가 발행된 날
    @Column(name = "published_at")
    private OffsetDateTime publishedAt;

    // API의 "enveloped_at", 뉴스가 수집된 날 (API가 뉴스를 수집한 시각)
    @Column(name = "enveloped_at")
    private OffsetDateTime envelopedAt;

    // API의 "dateline", 언론사 기준 뉴스의 출고 시각
    @Column(name = "dateline")
    private OffsetDateTime dateline;

    // API의 "provider", 언론사이름 (ex: 중앙일보, 한겨레)
    private String provider;

    // API의 "byline", 기자이름 (ex: 이태환)
    private String byline;

    //  API의 "provider_link_page" 원본 기사 URL (ex : https://www.joongang.co.kr/article/25334929)
    @Column(name = "provider_link_page")
    private String providerLinkPage;

    // 뉴스 원문을 요약한 요약본이 들어갈 예정
    @Lob
    @Column(name = "news_summary", columnDefinition = "LONGTEXT")
    private String summary;

    /*
    category라는 테이블을 여기서 만들고, newsid를 조인시켜서 관리함.
    즉 category는 	IT_과학>모바일 , [1001, 1002, 1003, 2005, 30201] 이렇게 관리됨
    -> 즉 attrbitue가 2개 (category, news_id)이고 각 카테고리에해당하는 news_id에 대한 정보를 지닌 테이블을 선언
    -> 그리고 이 뉴스본문에 대해 접근을 시도하면, 즉각적으로 이 카테고리 값을 같이 불러오겠다 (FetchType.EAGER)
    -> CATEGORY는 News 테이블과 별도로 존재하지만, News에 접근을 시도하면 즉각적으로 해당 뉴스와 관련된 카테고리 정보를 볼 수 있게 저장함.
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "news_category", joinColumns = @JoinColumn(name = "news_id"))
    @Column(name = "category")
    private List<String> category;

    /*
    category와 유사한 형태로 저장됨.
    -> category가 IT_과학>AI, IT_과학>모바일 이런식의 정보를 저장한다면, category_incident는
    -> category가 "재난" 이면 category_incident는 "화재", "산불", "지진" 과 같은 사건에 대한 정보를 제공
    -> 즉 category는 주제, category_incident는 구체적인 사건 내용을 다룬다.
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "news_incident_category", joinColumns = @JoinColumn(name = "news_id"))
    @Column(name = "incident_category")
    private List<String> categoryIncident;

    // 이미지의 url 주소
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "news_images", joinColumns = @JoinColumn(name = "news_id"))
    @Column(name = "image_url")
    private List<String> imageUrls;
    // 이러한 category와 category_incident는 뉴스토어에서 정제해서 우리에게 제공하는 데이터 형식임.

}