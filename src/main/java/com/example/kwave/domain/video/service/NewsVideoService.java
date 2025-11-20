package com.example.kwave.domain.video.service;

import com.example.kwave.domain.news.domain.News;
import com.example.kwave.domain.news.domain.repository.NewsRepository;
import com.example.kwave.domain.video.domain.VideoScene;
import com.example.kwave.domain.video.dto.SceneDto;
import com.example.kwave.domain.video.repository.VideoSceneRepository;
import com.example.kwave.domain.video.service.LlmSceneExtractorService;
import com.example.kwave.domain.video.service.RunwayGenerationService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class NewsVideoService {

    private final NewsRepository newsRepository;
    private final VideoSceneRepository videoSceneRepository;
    private final LlmSceneExtractorService llmSceneExtractorService;
    private final RunwayGenerationService runwayGenerationService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    public NewsVideoService(NewsRepository newsRepository, LlmSceneExtractorService llmSceneExtractorService, RunwayGenerationService runwayGenerationService, VideoSceneRepository videoSceneRepository) {
        this.newsRepository = newsRepository;
        this.llmSceneExtractorService = llmSceneExtractorService;
        this.runwayGenerationService = runwayGenerationService;
        this.videoSceneRepository = videoSceneRepository;
    }

    public String getScenesFromNews(String newsId) {
        News news = newsRepository.findByNewsId(newsId)
                .orElseThrow(() -> new RuntimeException("해당 ID의 뉴스가 존재하지 않습니다."));

        String combinedText = "Title: " + news.getTitle() + "\n\nContent:\n" + news.getContent();
        String sceneJson = llmSceneExtractorService.extractScenes(combinedText);

        // ✅ JSON → SceneDto 변환
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
            entity.setCreatedAt(LocalDateTime.now());
            videoSceneRepository.save(entity);
        }

        return sceneJson;
    }

    public List<String> generateVideoFromNews(String newsId) {
        List<VideoScene> scenes = videoSceneRepository.findByNewsId(newsId);
        if (scenes.isEmpty()) throw new RuntimeException("해당 뉴스의 Scene 정보가 없습니다.");

        List<String> videoUrls = new ArrayList<>();
        for (VideoScene scene : scenes) {
            log.info("🎞 Runway 호출 시작 — Prompt: [{}]", scene.getExtraPrompt());

            String prompt = scene.getExtraPrompt();
            if (prompt == null || prompt.isBlank()) {
                log.warn("⚠️ Scene {} 의 extraPrompt가 비어 있습니다. 건너뜁니다.", scene.getSceneIndex());
                continue;
            }

            String videoUrl = runwayGenerationService.generateVideo(prompt);

            // 3️⃣ 생성된 영상 URL을 DB에 저장
            scene.setVideoUrl(videoUrl);
            videoSceneRepository.save(scene);
            videoUrls.add(videoUrl);
        }

        return videoUrls;
    }
}