package com.example.kwave.domain.video.service;

import com.example.kwave.domain.news.domain.News;
import com.example.kwave.domain.news.repository.NewsRepository;
import com.example.kwave.domain.video.domain.VideoScene;
import com.example.kwave.domain.video.dto.SceneDto;
import com.example.kwave.domain.video.repository.VideoSceneRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.example.kwave.domain.video.service.LlmSceneExtractorService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class NewsVideoService {

    private final NewsRepository newsRepository;
    private final VideoSceneRepository videoSceneRepository;
    private final LlmSceneExtractorService llmSceneExtractorService;
    private final RunwayGenerationService runwayGenerationService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public NewsVideoService(NewsRepository newsRepository,
                            LlmSceneExtractorService llmSceneExtractorService,
                            RunwayGenerationService runwayGenerationService,
                            VideoSceneRepository videoSceneRepository) {
        this.newsRepository = newsRepository;
        this.llmSceneExtractorService = llmSceneExtractorService;
        this.runwayGenerationService = runwayGenerationService;
        this.videoSceneRepository = videoSceneRepository;
    }

    // ✅ 뉴스 기반 Scene 추출
    public String getScenesFromNews(String newsId) {
        String sceneJson = llmSceneExtractorService.extractScenesWithImages(newsId);

        List<SceneDto> scenes;
        try {
            scenes = objectMapper.readValue(sceneJson, new TypeReference<List<SceneDto>>() {});
        } catch (Exception e) {
            log.error("❌ Scene JSON 파싱 실패: {}", e.getMessage());
            throw new RuntimeException("Scene JSON 파싱 실패", e);
        }

        // ✅ Scene DB 저장
        for (SceneDto scene : scenes) {
            VideoScene entity = new VideoScene();
            entity.setNewsId(newsId);
            entity.setSceneIndex(scene.getSceneIndex());
            entity.setDescription(scene.getDescription());
            entity.setExtraPrompt(scene.getExtraPrompt());
            entity.setMatchedImageUrl(scene.getMatchedImageUrl());
            entity.setCreatedAt(LocalDateTime.now());
            videoSceneRepository.save(entity);
        }

        return sceneJson;
    }

    // ✅ 영상 생성 (I2V / T2V 분기)
    public List<String> generateVideoFromNews(String newsId) {
        List<VideoScene> scenes = videoSceneRepository.findByNewsId(newsId);
        if (scenes.isEmpty()) throw new RuntimeException("해당 뉴스의 Scene 정보가 없습니다.");

        List<String> videoUrls = new ArrayList<>();
        for (VideoScene scene : scenes) {
            String prompt = scene.getExtraPrompt();
            String imageUrl = scene.getMatchedImageUrl();

            if (prompt == null || prompt.isBlank()) {
                log.warn("⚠️ Scene {} 의 extraPrompt가 비어 있습니다. 건너뜁니다.", scene.getSceneIndex());
                continue;
            }

            String videoUrl;
            if (imageUrl != null && !imageUrl.isBlank()) {
                // ✅ 이미지 매칭된 경우 → I2V
                log.info("🎞 [I2V] Scene {} 이미지 매칭됨 → {}", scene.getSceneIndex(), imageUrl);
                videoUrl = runwayGenerationService.generateImageToVideo(prompt, imageUrl);
            } else {
                // ✅ 이미지 없는 경우 → T2V
                log.info("🎞 [T2V] Scene {} 이미지 없음 → 텍스트만 사용", scene.getSceneIndex());
                videoUrl = runwayGenerationService.generateTextToVideo(prompt);
            }

            scene.setVideoUrl(videoUrl);
            videoSceneRepository.save(scene);
            videoUrls.add(videoUrl);
        }

        return videoUrls;
    }
}