package com.example.kwave.domain.user.service;

import com.example.kwave.domain.news.dto.NewsEmbeddingDto;
import com.example.kwave.domain.news.service.NewsEmbeddingService;
import com.example.kwave.domain.recommend.domain.NewsReaction;
import com.example.kwave.domain.recommend.domain.ReactionType;
import com.example.kwave.domain.recommend.domain.repository.NewsReactionRepository;
import com.example.kwave.domain.user.domain.User;
import com.example.kwave.domain.user.domain.repository.UserRepository;
import com.example.kwave.global.util.FloatArrayConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserPreferenceBatchService {

    private final NewsReactionRepository newsReactionRepository;
    private final NewsEmbeddingService newsEmbeddingService;
    private final UserRepository userRepository;

    /**
     * 매일 새벽 전체 유저 선호 벡터 재계산
     * Dislike는 계산에 참여 X
     */
    public void rebuildAllUserPreferenceVectors() {
        log.info("== 모든 유저 선호 벡터 재계 배치 시작==");

        // Like + JustWatch만 조회
        List<NewsReaction> reactions = newsReactionRepository
                .findByReactionTypeIn(List.of(ReactionType.Like, ReactionType.JustWatch));

        if (reactions.isEmpty()) {
            log.info("선호 벡터를 계산할 반응 데이터가 없습니다.");
            return;
        }

        // userId 기준으로 그룹핑
        Map<UUID, List<NewsReaction>> reactionsByUser = reactions.stream()
                .collect(Collectors.groupingBy(NewsReaction::getUserId));

        int success = 0;
        int fail = 0;

        for (Map.Entry<UUID, List<NewsReaction>> entry : reactionsByUser.entrySet()) {
            UUID userId = entry.getKey();
            List<NewsReaction> userReactions = entry.getValue();

            try {
                rebuildUserPreferenceVector(userId, userReactions);
                success++;
            }
            catch (Exception e) {
                fail++;
                log.error("유저 선호 벡터 계산 실패 - userId = {}, error = {}", userId, e.getMessage());
            }
        }
        log.info("== 유저 선호 벡터 배치 완료 - 성공: {}, 실패: {}",  success, fail);
    }

    /**
     * 특정 유저 수동 재계산
     * @param userId
     */
    @Transactional
    public void rebuildUserPreferenceVector(UUID userId) {
        List<NewsReaction> userReactions = newsReactionRepository
                .findByUserIdAndReactionTypeIn(userId, List.of(ReactionType.Like, ReactionType.JustWatch));

        rebuildUserPreferenceVector(userId, userReactions);
    }

    /**
     * 실제 가중치 적용 + 벡터 평균 계산 로직
     */
    private void rebuildUserPreferenceVector(UUID userId, List<NewsReaction> userReactions) {
        if (userReactions == null || userReactions.isEmpty()) {
            log.info("유저 {}는 반응 데이터가 없어 선호 벡터를 갱신하지 않습니다.", userId);
            return;
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User Not Found:" + userId));

        float[] sumVector = null;
        float totalWeight = 0;

        for (NewsReaction reaction : userReactions) {
            String newsId = reaction.getNewsId();

            // 반응 타입별 가중치 적용
            float weight;
            if (reaction.getReactionType() ==  ReactionType.Like) {
                weight = 10f;
            }
            else if (reaction.getReactionType() ==  ReactionType.JustWatch) {
                weight = 3f;
            }
            else {
                continue;
            }

            // 뉴스 임베딩 조회
            Optional<NewsEmbeddingDto> optEmbedding = newsEmbeddingService.getEmbedding(newsId);
            if (optEmbedding.isEmpty()) {
                log.warn("NewsEmbedding 없음 - userId = {}, newsId = {}", userId, reaction.getNewsId());
                continue;
            }

            float[] vec = optEmbedding.get().getEmbedding();
            if (sumVector == null) {
                sumVector = new float[vec.length];
            }

            if (vec.length != sumVector.length) {
                throw new RuntimeException("임베딩 차원이 일치하지 않습니다. newsId = " + newsId);
            }

            // 가중치 합산
            for (int i = 0; i < vec.length; i++) {
                sumVector[i] += vec[i] * weight;
            }
            totalWeight += weight;
        }

        if (sumVector == null || totalWeight == 0f) {
            log.info("userId: {}에 대해 유효한 임베딩/가중치가 없어, 선호벡터를 갱신하지 않습니다.", userId);
            return;
        }

        // 가중 평균 벡터 계산
        for (int i = 0; i < sumVector.length; i++) {
            sumVector[i] /= totalWeight;
        }

        // preferenceVector에 저장
        user.setPreferenceVector(FloatArrayConverter.toBytes(sumVector));
        userRepository.save(user);

        log.info("유저 선호 벡터 갱신 완료 - userId: {}, 반응 개수: {}, totalWeight: {}", userId, userReactions.size(), totalWeight);
    }
}
