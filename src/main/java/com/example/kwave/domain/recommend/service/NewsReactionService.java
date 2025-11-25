package com.example.kwave.domain.recommend.service;

import com.example.kwave.domain.recommend.domain.NewsReaction;
import com.example.kwave.domain.recommend.domain.ReactionType;
import com.example.kwave.domain.recommend.domain.repository.NewsReactionRepository;
import com.example.kwave.domain.recommend.dto.NewsReactionReqDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@RequiredArgsConstructor
@Service
@Slf4j
public class NewsReactionService {

    private final NewsReactionRepository repository;

    /**
     * 뉴스 반응 저장 (좋아요/싫어요)
     * 반응이 있으면 업데이트, 없으면 새로 생성
     */
    @Transactional
    public void saveReaction(NewsReactionReqDto reqDto) {
        if (reqDto.getReactionType() == null) {
            log.info("뉴스 리액션 타입이 정의되지 않아 JustWatch로 설정함");
            reqDto.setReactionType(ReactionType.JustWatch);
        }

        log.info("리액션 저장 - userId: {}, newsId: {}, type: {}",
                reqDto.getUserId(), reqDto.getNewsId(), reqDto.getReactionType());

        NewsReaction reaction = repository.findByUserIdAndNewsId(reqDto.getUserId(), reqDto.getNewsId())
                .orElse(null);

        if (reaction != null) {
            ReactionType oldType = reaction.getReactionType();
            reaction.setReactionType(reqDto.getReactionType());
            reaction.setCreatedAt(LocalDateTime.now());
        }
        else {
            reaction = new NewsReaction();
            reaction.setUserId(reqDto.getUserId());
            reaction.setNewsId(reqDto.getNewsId());
            reaction.setReactionType(reqDto.getReactionType());
            reaction.setCreatedAt(LocalDateTime.now());

            log.info("Created new Reaction");
        }

        repository.save(reaction);
    }

    /**
     * 반응 삭제 (좋아요/싫어요)
     */
    @Transactional
    public void deleteReaction(NewsReactionReqDto reqDto) {
        log.info("Deleting reaction - userId: {}, newsId: {}", reqDto.getUserId(), reqDto.getNewsId());

        repository.findByUserIdAndNewsId(reqDto.getUserId(), reqDto.getNewsId())
                .ifPresentOrElse(
                        reaction -> {
                            repository.delete(reaction);
                            log.info("Reaction deleted successfully");
                        },
                        () -> log.warn("No reaction found to delete")
                );
    }
}
