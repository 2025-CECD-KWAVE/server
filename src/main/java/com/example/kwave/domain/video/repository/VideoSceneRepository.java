package com.example.kwave.domain.video.repository;

import com.example.kwave.domain.video.domain.VideoScene;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VideoSceneRepository extends JpaRepository<VideoScene, Long> {
    List<VideoScene> findByNewsId(String newsId);
}

