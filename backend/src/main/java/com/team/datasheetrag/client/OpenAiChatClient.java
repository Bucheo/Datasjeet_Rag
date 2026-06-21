package com.team.datasheetrag.client;

import com.team.datasheetrag.config.AppProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

/**
 * [담당: 이헌영 - OpenAI API 연동, 프롬프트 엔지니어링]
 * Chat Completions API 직접 호출.
 * 참고: https://platform.openai.com/docs/api-reference/chat
 */
@Component
public class OpenAiChatClient {

    private final RestClient restClient;
    private final AppProperties.OpenAi config;

    public OpenAiChatClient(RestClient.Builder builder, AppProperties appProperties) {
        this.config = appProperties.openai();
        this.restClient = builder
                .baseUrl(config.baseUrl())
                .defaultHeader("Authorization", "Bearer " + config.apiKey())
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    /**
     * 시스템 프롬프트 + 사용자 질문을 보내고 답변 텍스트를 받는다.
     * temperature=0 으로 고정해 사실 기반 응답의 일관성을 높인다 (할루시네이션 억제 목적).
     */
    @SuppressWarnings("unchecked")
    public String chat(String systemPrompt, String userMessage) {
        Map<String, Object> requestBody = Map.of(
                "model", config.chatModel(),
                "temperature", 0,
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userMessage)
                )
        );

        Map<String, Object> response = restClient.post()
                .uri("/chat/completions")
                .body(requestBody)
                .retrieve()
                .body(Map.class);

        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
        return (String) message.get("content");
    }
}
