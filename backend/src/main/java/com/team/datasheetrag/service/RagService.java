package com.team.datasheetrag.service;

import com.team.datasheetrag.client.ChromaClient;
import com.team.datasheetrag.client.OpenAiChatClient;
import com.team.datasheetrag.config.AppProperties;
import com.team.datasheetrag.dto.ChatDtos;
import com.team.datasheetrag.dto.SourceItem;
import com.team.datasheetrag.dto.TextChunk;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * [담당: 이헌영 - OpenAI API 연동 및 RAG 체인]
 *
 * 추진 일정 9주차: 검색된 원문과 질문을 결합하는 RAG 통합 체인 설계 및 AI 답변 생성.
 *
 * 흐름: 사용자 질문 → 벡터 검색(ChromaClient) → 시스템 프롬프트 구성(PromptService)
 *      → LLM 호출(OpenAiChatClient) → 출처 포함 응답
 */
@Service
public class RagService {

    private final ChromaClient chromaClient;
    private final OpenAiChatClient chatClient;
    private final PromptService promptService;
    private final int topK;

    public RagService(ChromaClient chromaClient, OpenAiChatClient chatClient,
                       PromptService promptService, AppProperties appProperties) {
        this.chromaClient = chromaClient;
        this.chatClient = chatClient;
        this.promptService = promptService;
        this.topK = appProperties.rag().topK();
    }

    public ChatDtos.ChatResponse ask(String question, String documentFilter) {
        List<TextChunk> chunks = chromaClient.query(question, topK);

        if (documentFilter != null && !documentFilter.isBlank()) {
            chunks = chunks.stream()
                    .filter(c -> documentFilter.equals(c.source()))
                    .toList();
        }

        if (chunks.isEmpty()) {
            return new ChatDtos.ChatResponse(
                    "제공된 문서에서 해당 정보를 찾을 수 없습니다. 관련 데이터시트를 먼저 업로드해주세요.",
                    List.of()
            );
        }

        String systemPrompt = promptService.buildSystemPrompt(chunks);
        String answer = chatClient.chat(systemPrompt, question);

        List<SourceItem> sources = deduplicateSources(chunks);
        return new ChatDtos.ChatResponse(answer, sources);
    }

    /** 같은 문서/페이지에서 여러 chunk가 검색된 경우 출처 표시에서는 중복을 제거한다. */
    private List<SourceItem> deduplicateSources(List<TextChunk> chunks) {
        Map<String, SourceItem> unique = new LinkedHashMap<>();
        for (TextChunk chunk : chunks) {
            String key = chunk.source() + "#" + chunk.page();
            unique.putIfAbsent(key, new SourceItem(chunk.source(), chunk.page()));
        }
        return List.copyOf(unique.values());
    }
}
