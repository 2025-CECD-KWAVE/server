package com.example.kwave.domain.video.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "video_scene")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class VideoScene {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String newsId;          // 뉴스 고유 ID
    private int sceneIndex;         // 장면 순서
    private String description;     // 장면 설명
    private String extraPrompt;     // 영상 생성용 프롬프트
    @Column(length = 1000)
    private String videoUrl;        // Runway 영상 결과 URL
    private LocalDateTime createdAt;
}