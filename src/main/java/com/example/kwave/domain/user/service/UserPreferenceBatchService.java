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
     */
    public void rebuildAllUserPreferenceVectors() {
        log.info("== 모든 유저 선호 벡터 재계산 배치 시작 ==");

        // Like, JustWatch만 조회
        List<NewsReaction> reactions = newsReactionRepository
                .findByReactionTypeIn(List.of(ReactionType.Like, ReactionType.JustWatch));

        if (reactions.isEmpty()) {
            log.info("선호 벡터를 계산할 반응 데이터가 없습니다.");
            return;
        }

        Map<UUID, List<NewsReaction>> reactionsByUser = reactions.stream()
                .collect(Collectors.groupingBy(NewsReaction::getUserId));

        int success = 0;
        int fail = 0;

        for (Map.Entry<UUID, List<NewsReaction>> entry : reactionsByUser.entrySet()) {
            try {
                rebuildUserPreferenceVector(entry.getKey(), entry.getValue());
                success++;
            } catch (Exception e) {
                fail++;
                log.error("유저 선호 벡터 계산 실패 - userId = {}, error = {}", entry.getKey(), e.getMessage());
            }
        }
        log.info("== 배치 완료 - 성공: {}, 실패: {}", success, fail);
    }

    @Transactional
    public void rebuildUserPreferenceVector(UUID userId) {
        // 단건 조회 시에도 Dislike는 제외하고 가져옴
        List<NewsReaction> userReactions = newsReactionRepository
                .findByUserIdAndReactionTypeIn(userId,
                        List.of(ReactionType.Like, ReactionType.JustWatch));

        rebuildUserPreferenceVector(userId, userReactions);
    }

    /**
     * 가중 평균
     */
    private void rebuildUserPreferenceVector(UUID userId, List<NewsReaction> userReactions) {
        if (userReactions == null || userReactions.isEmpty()) {
            log.info("유저 {}는 긍정 반응 데이터가 없어 선호 벡터를 갱신하지 않습니다.", userId);
            return;
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User Not Found:" + userId));

        float[] positiveVectorSum = null;
        float positiveWeightSum = 0f;
        int dimension = 0;

        // 벡터 합계 구하기
        for (NewsReaction reaction : userReactions) {

            // 혹시라도 Dislike가 섞여 들어오면 무시
            if (reaction.getReactionType() == ReactionType.Dislike) {
                continue;
            }

            String newsId = reaction.getNewsId();
            Optional<NewsEmbeddingDto> optEmbedding = newsEmbeddingService.getEmbedding(newsId);

            if (optEmbedding.isEmpty()) continue;

            float[] vec = optEmbedding.get().getEmbedding();

            // 차원 초기화 및 검증
            if (dimension == 0) {
                dimension = vec.length;
            } else if (dimension != vec.length) {
                continue;
            }

            ReactionType type = reaction.getReactionType();

            // 긍정 그룹 가중치 합산
            float weight = (float) type.getWeight(); // Enum에 정의된 weight 활용

            if (positiveVectorSum == null) {
                positiveVectorSum = new float[dimension];
            }

            for (int i = 0; i < dimension; i++) {
                positiveVectorSum[i] += vec[i] * weight;
            }
            positiveWeightSum += weight;
        }

        // 유효성 검증
        if (positiveVectorSum == null || positiveWeightSum == 0f) {
            log.info("유효한 임베딩 데이터가 없어 벡터 생성을 중단합니다. userId = {}", userId);
            return;
        }

        // 최종 벡터 계산 (단순 가중 평균)
        float[] finalVector = new float[dimension];
        double magnitudeSq = 0.0;

        for (int i = 0; i < dimension; i++) {
            finalVector[i] = positiveVectorSum[i] / positiveWeightSum;

            magnitudeSq += finalVector[i] * finalVector[i];
        }

        // 정규화를 통해 벡터의 크기를 1로 만듦
        if (magnitudeSq > 0) {
            float magnitude = (float) Math.sqrt(magnitudeSq);
            for (int i = 0; i < dimension; i++) {
                finalVector[i] /= magnitude;
            }
        }

        // DB 저장
        user.setPreferenceVector(FloatArrayConverter.toBytes(finalVector));
        userRepository.save(user);

        log.info("유저 선호 벡터 갱신 완료 - userId: {}, TotalWeight: {}", userId, positiveWeightSum);
    }
}