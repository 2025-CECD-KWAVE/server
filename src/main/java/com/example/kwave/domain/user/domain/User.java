package com.example.kwave.domain.user.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "user")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @Column(name = "user_name", nullable = false)
    private String username;

    @Column(name = "user_email", nullable = false, unique = true)
    private String email;

    @Column(name = "user_passwd", nullable = false)
    private String password;

    //사용자 국적
    @Column(name = "user_nationality", nullable = false)
    private String nationality;

    //사용 언어
    @Column(name = "user_language", nullable = false)
    private String language;

    //회원가입 시 선택한 관심 카테고리들
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_preferred_category", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "category")
    private List<String> preferredCategories;

    //사용자가 실제로 본 뉴스들의 카테고리 이력
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_viewed_category", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "category")
    private List<String> viewedCategories;
}
