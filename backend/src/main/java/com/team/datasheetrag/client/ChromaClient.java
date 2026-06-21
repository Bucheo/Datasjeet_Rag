package com.team.datasheetrag.client;

import com.team.datasheetrag.config.AppProperties;
import com.team.datasheetrag.dto.TextChunk;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.*;

/**
 * [담당: 신승민 - 벡터 DB 구축/관리]
 *
 * Chroma는 별도의 Python 프로세스로 실행되는 외부 벡터 DB이며,
 * Spring Boot에서는 LangChain 없이 Chroma v2 REST API를 RestClient로 직접 호출한다.
 *
 * 사전 준비 (터미널에서 1회 실행, Spring Boot 앱과는 별도 프로세스):
 *   pip install chromadb
 *   chroma run --path ./chroma-data --port 8000
 *
 * v2 API 경로 구조: /api/v2/tenants/{tenant}/databases/{database}/collections/...
 * 참고: https://docs.trychroma.com/reference/chroma-api
 */
@Component
public class ChromaClient {

    private final RestClient restClient;
    private final AppProperties.Chroma config;
    private final OpenAiEmbeddingClient embeddingClient;

    private String cachedCollectionId; // 매 요청마다 collection 이름→id 조회를 피하기 위한 캐시

    public ChromaClient(RestClient.Builder builder, AppProperties appProperties, OpenAiEmbeddingClient embeddingClient) {
        this.config = appProperties.chroma();
        this.embeddingClient = embeddingClient;
        this.restClient = builder.baseUrl(config.baseUrl()).build();
    }

    private String basePath() {
        return "/api/v2/tenants/" + config.tenant() + "/databases/" + config.database();
    }

    /** collection이 없으면 생성하고, 있으면 그 id를 반환한다 (get_or_create 패턴). */
    @SuppressWarnings("unchecked")
    private synchronized String getOrCreateCollectionId() {
        if (cachedCollectionId != null) {
            return cachedCollectionId;
        }

        Map<String, Object> body = new HashMap<>();
        body.put("name", config.collectionName());
        body.put("get_or_create", true);

        Map<String, Object> response = restClient.post()
                .uri(basePath() + "/collections")
                .body(body)
                .retrieve()
                .body(Map.class);

        cachedCollectionId = (String) response.get("id");
        return cachedCollectionId;
    }

    /**
     * Chunk 목록을 OpenAI로 임베딩한 뒤 Chroma에 저장한다.
     * id는 "파일명#페이지#순번" 조합으로 생성해 같은 문서를 재업로드해도 충돌하지 않게 한다.
     */
    public void addChunks(List<TextChunk> chunks) {
        if (chunks.isEmpty()) return;
        String collectionId = getOrCreateCollectionId();

        List<String> documents = new ArrayList<>();
        List<Map<String, Object>> metadatas = new ArrayList<>();
        List<String> ids = new ArrayList<>();

        for (int i = 0; i < chunks.size(); i++) {
            TextChunk chunk = chunks.get(i);
            documents.add(chunk.content());
            metadatas.add(chunk.toMetadata());
            ids.add(chunk.source() + "#" + chunk.page() + "#" + i + "#" + UUID.randomUUID());
        }

        List<List<Double>> embeddings = embeddingClient.embedBatch(documents);

        Map<String, Object> body = new HashMap<>();
        body.put("ids", ids);
        body.put("embeddings", embeddings);
        body.put("documents", documents);
        body.put("metadatas", metadatas);

        restClient.post()
                .uri(basePath() + "/collections/" + collectionId + "/add")
                .body(body)
                .retrieve()
                .toBodilessEntity();
    }

    /**
     * 질의어와 가장 유사한 chunk를 검색한다.
     * 반환값은 [{"content": ..., "source": ..., "page": ...}, ...] 형태로 가공해서 돌려준다.
     */
    @SuppressWarnings("unchecked")
    public List<TextChunk> query(String questionText, int topK) {
        String collectionId = getOrCreateCollectionId();
        List<Double> queryEmbedding = embeddingClient.embed(questionText);

        Map<String, Object> body = new HashMap<>();
        body.put("query_embeddings", List.of(queryEmbedding));
        body.put("n_results", topK);
        body.put("include", List.of("documents", "metadatas"));

        Map<String, Object> response = restClient.post()
                .uri(basePath() + "/collections/" + collectionId + "/query")
                .body(body)
                .retrieve()
                .body(Map.class);

        // Chroma 응답은 질의어 1개를 보냈어도 한 단계 더 감싸진 배열로 온다: documents[0] = 첫 질의의 결과 목록
        List<List<String>> documentsByQuery = (List<List<String>>) response.get("documents");
        List<List<Map<String, Object>>> metadatasByQuery = (List<List<Map<String, Object>>>) response.get("metadatas");

        if (documentsByQuery == null || documentsByQuery.isEmpty()) {
            return List.of();
        }

        List<String> documents = documentsByQuery.get(0);
        List<Map<String, Object>> metadatas = metadatasByQuery.get(0);

        List<TextChunk> results = new ArrayList<>();
        for (int i = 0; i < documents.size(); i++) {
            Map<String, Object> meta = metadatas.get(i);
            String source = String.valueOf(meta.get("source"));
            int page = meta.get("page") != null ? ((Number) meta.get("page")).intValue() : -1;
            results.add(new TextChunk(documents.get(i), source, page));
        }
        return results;
    }

    /** 특정 문서를 재업로드하거나 삭제할 때, 기존 chunk를 모두 제거한다. */
    public void deleteBySource(String filename) {
        String collectionId = getOrCreateCollectionId();
        Map<String, Object> body = Map.of("where", Map.of("source", filename));

        restClient.post()
                .uri(basePath() + "/collections/" + collectionId + "/delete")
                .body(body)
                .retrieve()
                .toBodilessEntity();
    }
}
