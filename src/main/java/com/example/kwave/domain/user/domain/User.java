package com.example.kwave.domain.user.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "user")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    // 로그인 시 이용하는 아이디 (클라이언트에서 email 적으라고 할 수도)
    @Column(name = "user_name", nullable = false, unique = true)
    private String username;

    // 프로그램 사용 시 사용하는 닉네임
    @Column(name = "nickname", nullable = false, unique = true)
    private String nickname;

    @Column(name = "user_passwd", nullable = false)
    private String password;

    // 사용자 국적
    @Column(name = "user_nationality", nullable = false)
    private String nationality;

    // 사용 언어
    @Column(name = "user_language", nullable = false)
    private String language;

    // 회원가입 시 선택한 관심 카테고리들
    // userId | category | weight 테이블 형식
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_preferred_category", joinColumns = @JoinColumn(name = "user_id"))
    @MapKeyColumn(name = "category")
    @Column(name = "weight")
    private Map<String, Integer> preferredCategories;

    // 사용자가 실제로 본 뉴스들의 카테고리 이력
    // 테이블 형식은 위와 동일
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_viewed_category", joinColumns = @JoinColumn(name = "user_id"))
    @MapKeyColumn(name = "category")
    @Column(name = "weight")
    private Map<String, Integer> viewedCategories;
}
