package com.team.datasheetrag.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public class ChatDtos {

    public record ChatRequest(
            @NotBlank(message = "질문 내용을 입력해주세요.") String question,
            String documentFilter // 특정 문서로 검색 범위를 제한할 때 파일명 (선택)
    ) {}

    public record ChatResponse(String answer, List<SourceItem> sources) {}
}
