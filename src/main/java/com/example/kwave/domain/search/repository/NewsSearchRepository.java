package com.example.kwave.domain.search.repository;

import com.example.kwave.domain.ai.domain.NewsVectorDoc;
import com.example.kwave.domain.ai.service.OpenAiService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opensearch.client.Request;
import org.opensearch.client.Response;
import org.opensearch.client.RestHighLevelClient;
import org.springframework.stereotype.Repository;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 뉴스 벡터 검색 Repository
 * OpenSearch의 KNN(K-Nearest Neighbors) 기능을 사용하여 의미론적 검색 수행
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class NewsSearchRepository {

    private final RestHighLevelClient opensearchClient;  // OpenSearch 클라이언트
    private final OpenAiService openAiService;            // OpenAI 임베딩 서비스
    private final ObjectMapper objectMapper;              // JSON 변환 유틸

    private static final String INDEX_NAME = "news_vectors";

    /**
     * 검색어를 기반으로 유사한 뉴스를 검색
     *
     * @param searchQuery 사용자 검색어 (예: "BTS", "케이팝 콘서트")
     * @param page 페이지 번호 (0부터 시작)
     * @param size 페이지당 결과 개수
     * @return 검색 결과 (문서 리스트, 유사도 점수, 총 개수 등)
     */
    public SearchResult searchNews(String searchQuery, int page, int size) {
        try {
            // 검색어를 벡터로 변환 (OpenAI text-embedding-3-small 사용)
            log.info("검색 시작: '{}'", searchQuery);
            float[] queryVector = openAiService.embed(searchQuery);

            // OpenSearch KNN 쿼리 생성 (수정됨: searchQuery를 함께 전달)
            // 생성된 벡터와 텍스트 키워드를 사용하여 하이브리드 검색 쿼리 생성
            String queryJson = buildKnnQuery(queryVector, searchQuery, page, size);

            // OpenSearch에 검색 요청
            Request request = new Request("POST", "/" + INDEX_NAME + "/_search");
            request.setJsonEntity(queryJson);
            Response response = opensearchClient.getLowLevelClient().performRequest(request);

            // 응답 본문 읽기
            String responseBody = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent(), StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));

            // JSON 응답을 Java 객체로 변환하여 반환
            return convertToSearchResult(responseBody);

        } catch (Exception e) {
            log.error("검색 실패: {}", searchQuery, e);
            throw new RuntimeException("뉴스 검색 중 오류가 발생했습니다", e);
        }
    }

    /**
     * Vector로 검색
     */
    public KnnResult searchByVector(float[] vector, List<String> dislikedNewsIds, int page, int size) {

        try {
            // knn + dislike filter 쿼리 Json으로 만들기
            String queryJson = buildKnnQueryWithFilter(vector, dislikedNewsIds, page, size);

            // opensearch에 요청
            Request request = new Request("POST", "/" + INDEX_NAME + "/_search");
            request.setJsonEntity(queryJson);
            Response response = opensearchClient.getLowLevelClient().performRequest(request);

            // response 본문 읽기
            String responseBody = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent(), StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));

            SearchResult sr = convertToSearchResult(responseBody);

            return new KnnResult(
                    sr.getDocuments(),
                    sr.getScores(),
                    sr.getTotalHits(),
                    sr.getMaxScore()
            );
        }
        catch (Exception e) {
            log.error("KNN 벡터 검색 실패", e);
            throw new RuntimeException("추천 검색 중 오류가 발생했습니다.", e);
        }
    }

    private String buildKnnQueryWithFilter(float[] vector, List<String> dislikedNewsIds, int page, int size) {

        try {
            Map<String, Object> root = new HashMap<>();
            root.put("size", size);
            root.put("from", page * size);

            // knn part
            Map<String, Object> knn = new HashMap<>();
            Map<String, Object> embeddingKnn = new HashMap<>();
            embeddingKnn.put("vector", vector);
            embeddingKnn.put("k", Math.max(100, (page + 1) * size));
            knn.put("embedding", embeddingKnn);
            Map<String, Object> query = new HashMap<>();
            query.put("knn", knn);

            // Dislike Filter
            if (dislikedNewsIds != null && !dislikedNewsIds.isEmpty()) {
                Map<String, Object> terms = new HashMap<>();
                terms.put("newsId", dislikedNewsIds);
                Map<String, Object> mustNot = new HashMap<>();
                mustNot.put("terms", terms);

                // knn + bool을 어떻게 조합해 사용하는 지는 opensearch 버전에 따라 상이할 수 있음
                Map<String, Object> boolQuery = new HashMap<>();
                boolQuery.put("must", List.of(Map.of("knn", knn)));
                boolQuery.put("must_not", List.of(mustNot));

                query.clear();
                query.put("bool", boolQuery);
            }
            root.put("query", query);
            root.put("_source", Map.of("excludes", new String[]{"embedding", "_class"}));

            return objectMapper.writeValueAsString(root);
        }
        catch (Exception e) {
            log.error("추천 KNN Query 생성 실패", e);
            throw new RuntimeException("추천 쿼리 생성 중 에러가 발생했습니다.", e);
        }
    }

    /**
     * OpenSearch KNN + Keyword 하이브리드 쿼리 생성
     *
     * @param queryVector 검색어의 임베딩 벡터
     * @param searchQuery 사용자 텍스트 검색어 (추가됨)
     * @param page 페이지 번호
     * @param size 페이지당 결과 개수
     * @return JSON 형태의 쿼리 문자열
     */
    private String buildKnnQuery(float[] queryVector, String searchQuery, int page, int size) {
        try {
            Map<String, Object> root = new HashMap<>();

            // 페이징 설정
            root.put("size", size);
            root.put("from", page * size);

            // --- 쿼리 구조 변경 시작 (Bool Query: KNN + Match) ---
            Map<String, Object> query = new HashMap<>();
            Map<String, Object> bool = new HashMap<>();
            List<Map<String, Object>> should = new ArrayList<>();

            // KNN (Vector) 검색 부분
            Map<String, Object> knnWrapper = new HashMap<>();
            Map<String, Object> knn = new HashMap<>();
            Map<String, Object> embeddingKnn = new HashMap<>();
            embeddingKnn.put("vector", queryVector);
            embeddingKnn.put("k", Math.max(100, (page + 1) * size));
            knn.put("embedding", embeddingKnn);
            knnWrapper.put("knn", knn);

            should.add(knnWrapper); // Vector 검색 추가

            // Keyword (Multi-match) 검색 부분
            // 검색어가 있을 때만 키워드 매칭을 추가하여 점수를 보정합니다.
            if (searchQuery != null && !searchQuery.trim().isEmpty()) {
                Map<String, Object> matchWrapper = new HashMap<>();
                Map<String, Object> multiMatch = new HashMap<>();

                multiMatch.put("query", searchQuery);

                // Summary에 있으면 점수 3배, Content에 있으면 1배
                multiMatch.put("fields", List.of("newsSummary^3.0", "newsContent^0.3"));

                // phrase_prefix: "~~"로 시작하는 단어
                multiMatch.put("type", "phrase_prefix");

                matchWrapper.put("multi_match", multiMatch);
                should.add(matchWrapper); // Keyword 검색 추가
            }

            // should 조건 중 하나라도 만족하면 결과 반환 (Vector OR Keyword)
            bool.put("should", should);
            bool.put("minimum_should_match", 1);

            query.put("bool", bool);
            root.put("query", query);
            // --- 쿼리 구조 변경 끝 ---

            // 응답에서 제외할 필드 설정
            root.put("_source", Map.of("excludes", new String[]{"embedding", "_class"}));

            return objectMapper.writeValueAsString(root);

        } catch (Exception e) {
            log.error("쿼리 생성 실패", e);
            throw new RuntimeException("쿼리 생성 중 오류가 발생했습니다", e);
        }
    }

    /**
     * OpenSearch 응답을 Java 객체로 변환
     *
     * @param responseBody OpenSearch의 JSON 응답
     * @return 변환된 검색 결과 객체
     */
    @SuppressWarnings("unchecked")
    private SearchResult convertToSearchResult(String responseBody) {
        try {
            // JSON 문자열을 Map으로 파싱
            Map<String, Object> responseMap = objectMapper.readValue(responseBody, Map.class);
            Map<String, Object> hits = (Map<String, Object>) responseMap.get("hits");

            // 검색 결과가 없는 경우 빈 결과 반환
            if (hits == null) {
                log.warn("검색 결과 없음");
                return new SearchResult(List.of(), List.of(), 0L, 0.0f);
            }

            // 실제 문서 목록 추출
            List<Map<String, Object>> hitsList = (List<Map<String, Object>>) hits.get("hits");
            if (hitsList == null || hitsList.isEmpty()) {
                return new SearchResult(List.of(), List.of(), 0L, 0.0f);
            }

            // 전체 결과 개수 추출
            Map<String, Object> total = (Map<String, Object>) hits.get("total");
            long totalHits = total != null ? ((Number) total.get("value")).longValue() : 0L;

            // 최고 유사도 점수 추출
            Object maxScoreObj = hits.get("max_score");
            float maxScore = (maxScoreObj instanceof Number) ? ((Number) maxScoreObj).floatValue() : 1.0f;

            // 각 문서를 NewsVectorDoc 객체로 변환
            List<NewsVectorDoc> documents = new ArrayList<>();
            List<Float> scores = new ArrayList<>();

            for (int i = 0; i < hitsList.size(); i++) {
                try {
                    Map<String, Object> hit = hitsList.get(i);
                    Object sourceObj = hit.get("_source");

                    // _source 필드 검증
                    if (!(sourceObj instanceof Map)) {
                        continue;
                    }

                    Map<String, Object> source = (Map<String, Object>) sourceObj;

                    // 혹시 모를 경우를 대비해 불필요한 필드 제거
                    source.remove("embedding");
                    source.remove("_class");

                    // 유사도 점수 추출
                    Object scoreObj = hit.get("_score");
                    float score = (scoreObj instanceof Number) ? ((Number) scoreObj).floatValue() : 0.0f;

                    // NewsVectorDoc 객체 생성
                    NewsVectorDoc doc = NewsVectorDoc.builder()
                            .id(getStringValue(source, "id"))
                            .newsId(getStringValue(source, "newsId"))
                            .newsContent(getStringValue(source, "newsContent"))
                            .newsSummary(getStringValue(source, "newsSummary"))
                            .build();

                    documents.add(doc);
                    scores.add(score);

                } catch (Exception e) {
                    // 개별 문서 변환 실패 시 해당 문서만 건너뛰고 계속 진행
                    log.error("{}번째 문서 변환 실패", i, e);
                }
            }

            log.info("검색 완료: {} 문서 (전체 {}개)", documents.size(), totalHits);

            return new SearchResult(documents, scores, totalHits, maxScore);

        } catch (Exception e) {
            log.error("응답 파싱 실패", e);
            throw new RuntimeException("검색 결과 변환 중 오류가 발생했습니다", e);
        }
    }

    /**
     * Map에서 String 값을 안전하게 추출하는 유틸 메서드
     * null 체크를 통해 NullPointerException 방지
     */
    private String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }

    /**
     * 검색 결과를 담는 DTO
     *
     * documents 검색된 뉴스 문서 리스트
     * scores 각 문서의 유사도 점수 (높을수록 더 유사)
     * totalHits 전체 검색 결과 개수
     * maxScore 최고 유사도 점수 (정규화에 사용)
     */
    @lombok.Getter
    @lombok.AllArgsConstructor
    public static class SearchResult {
        private List<NewsVectorDoc> documents;
        private List<Float> scores;
        private long totalHits;
        private float maxScore;
    }

    @lombok.Getter
    @lombok.AllArgsConstructor
    public static class KnnResult {
        private List<NewsVectorDoc> documents;
        private List<Float> scores;
        private long totalHits;
        private float maxScore;
    }
}