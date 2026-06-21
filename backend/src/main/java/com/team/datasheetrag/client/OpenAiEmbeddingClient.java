package com.team.datasheetrag.client;

import com.team.datasheetrag.config.AppProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

/**
 * [담당: 이헌영 - OpenAI API 연동]
 * LangChain 없이 OpenAI REST API를 RestClient로 직접 호출한다.
 * 참고: https://platform.openai.com/docs/api-reference/embeddings
 */
@Component
public class OpenAiEmbeddingClient {

    private final RestClient restClient;
    private final AppProperties.OpenAi config;

    public OpenAiEmbeddingClient(RestClient.Builder builder, AppProperties appProperties) {
        this.config = appProperties.openai();
        this.restClient = builder
                .baseUrl(config.baseUrl())
                .defaultHeader("Authorization", "Bearer " + config.apiKey())
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    /** 텍스트 1개를 임베딩 벡터로 변환한다. */
    public List<Double> embed(String text) {
        return embedBatch(List.of(text)).get(0);
    }

    /** 여러 텍스트를 한 번의 API 호출로 임베딩 변환한다 (비용/속도 최적화). */
    @SuppressWarnings("unchecked")
    public List<List<Double>> embedBatch(List<String> texts) {
        Map<String, Object> requestBody = Map.of(
                "model", config.embeddingModel(),
                "input", texts
        );

        Map<String, Object> response = restClient.post()
                .uri("/embeddings")
                .body(requestBody)
                .retrieve()
                .body(Map.class);

        List<Map<String, Object>> data = (List<Map<String, Object>>) response.get("data");
        return data.stream()
                .map(item -> (List<Double>) item.get("embedding"))
                .toList();
    }
}
