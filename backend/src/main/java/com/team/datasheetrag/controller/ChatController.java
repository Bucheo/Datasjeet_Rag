package com.team.datasheetrag.controller;

import com.team.datasheetrag.dto.ChatDtos;
import com.team.datasheetrag.service.ChatHistoryService;
import com.team.datasheetrag.service.RagService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** RAG Q&A 엔드포인트. 질문 → RagService.ask() 호출 → 출처 포함 답변 반환, 동시에 이력 저장. */
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final RagService ragService;
    private final ChatHistoryService chatHistoryService;

    public ChatController(RagService ragService, ChatHistoryService chatHistoryService) {
        this.ragService = ragService;
        this.chatHistoryService = chatHistoryService;
    }

    @PostMapping
    public ChatDtos.ChatResponse chat(@Valid @RequestBody ChatDtos.ChatRequest request) {
        ChatDtos.ChatResponse response = ragService.ask(request.question(), request.documentFilter());
        chatHistoryService.save(request.documentFilter(), request, response);
        return response;
    }

    // NOTE(이헌영): 계획서의 '실시간으로 출력되는 스트리밍 대화창' 요구사항 대응.
    // 현재는 동기 응답이며, 추후 SseEmitter 또는 ResponseBodyEmitter로 교체 검토.
    // 프론트엔드 ChatWindow.tsx도 이에 맞춰 fetch stream 또는 EventSource로 전환 필요.
}
