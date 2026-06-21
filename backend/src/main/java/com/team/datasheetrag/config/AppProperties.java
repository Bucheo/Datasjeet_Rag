package com.team.datasheetrag.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * application.yml의 app.* 항목을 자바 객체로 바인딩.
 * 각 RestController/Service에서 @Autowired로 주입받아 사용한다.
 */
@ConfigurationProperties(prefix = "app")
public record AppProperties(
        Cors cors,
        Upload upload,
        OpenAi openai,
        Chroma chroma,
        Rag rag
) {
    public record Cors(String allowedOrigin) {}

    public record Upload(String dir) {}

    public record OpenAi(String apiKey, String baseUrl, String chatModel, String embeddingModel) {}

    public record Chroma(String baseUrl, String tenant, String database, String collectionName) {}

    public record Rag(int topK, int chunkSize, int chunkOverlap) {}
}
