package com.team.datasheetrag.dto;

import java.util.Map;

/**
 * PDF에서 추출/분할된 텍스트 조각 1개.
 * source(파일명), page(페이지 번호) 메타데이터를 함께 들고 다니며,
 * 이 메타데이터가 그대로 Chroma metadata → 답변의 출처 표시까지 이어진다.
 */
public record TextChunk(String content, String source, int page) {

    public Map<String, Object> toMetadata() {
        return Map.of("source", source, "page", page);
    }
}
