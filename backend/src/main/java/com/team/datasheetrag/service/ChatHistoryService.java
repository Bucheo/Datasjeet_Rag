package com.team.datasheetrag.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team.datasheetrag.dto.ChatDtos;
import com.team.datasheetrag.entity.ChatMessage;
import com.team.datasheetrag.repository.ChatMessageRepository;
import org.springframework.stereotype.Service;

/** 계획서 요구사항: '채팅 기록을 저장'. 질문/답변 각 1건씩 RDBMS에 남긴다. */
@Service
public class ChatHistoryService {

    private final ChatMessageRepository chatMessageRepository;
    private final ObjectMapper objectMapper;

    public ChatHistoryService(ChatMessageRepository chatMessageRepository, ObjectMapper objectMapper) {
        this.chatMessageRepository = chatMessageRepository;
        this.objectMapper = objectMapper;
    }

    public void save(String documentFilter, ChatDtos.ChatRequest request, ChatDtos.ChatResponse response) {
        ChatMessage userMsg = new ChatMessage();
        userMsg.setRole("user");
        userMsg.setContent(request.question());
        userMsg.setDocumentFilter(documentFilter);
        chatMessageRepository.save(userMsg);

        ChatMessage assistantMsg = new ChatMessage();
        assistantMsg.setRole("assistant");
        assistantMsg.setContent(response.answer());
        assistantMsg.setDocumentFilter(documentFilter);
        try {
            assistantMsg.setSourcesJson(objectMapper.writeValueAsString(response.sources()));
        } catch (Exception e) {
            assistantMsg.setSourcesJson("[]");
        }
        chatMessageRepository.save(assistantMsg);
    }
}
