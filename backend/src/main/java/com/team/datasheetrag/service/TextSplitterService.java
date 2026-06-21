package com.team.datasheetrag.service;

import com.team.datasheetrag.config.AppProperties;
import com.team.datasheetrag.dto.TextChunk;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * [담당: 신승민 - 텍스트 분할 전략]
 *
 * 추진 일정 6주차: 추출된 텍스트의 문단 단위(Chunk) 분리.
 *
 * LangChain의 RecursiveCharacterTextSplitter와 동일한 아이디어를 직접 구현:
 * 문자 수 기준으로 자르되, overlap을 둬서 chunk 경계에서 문맥이 끊기는 문제를 완화한다.
 * 레지스터 맵/핀 사양 표가 chunk 경계에서 끊기면 검색 품질이 떨어지므로,
 * chunkSize/overlap 값은 실제 데이터시트로 테스트하며 튜닝할 것 (application.yml의 app.rag 항목).
 */
@Service
public class TextSplitterService {

    private final int chunkSize;
    private final int chunkOverlap;

    public TextSplitterService(AppProperties appProperties) {
        this.chunkSize = appProperties.rag().chunkSize();
        this.chunkOverlap = appProperties.rag().chunkOverlap();
    }

    /** 페이지 단위 TextChunk 리스트를 검색에 적합한 작은 chunk로 재분할한다. */
    public List<TextChunk> split(List<TextChunk> pages) {
        List<TextChunk> result = new ArrayList<>();
        for (TextChunk page : pages) {
            result.addAll(splitSingleText(page.content(), page.source(), page.page()));
        }
        return result;
    }

    private List<TextChunk> splitSingleText(String text, String source, int page) {
        List<TextChunk> chunks = new ArrayList<>();
        if (text.isBlank()) return chunks;

        int start = 0;
        int length = text.length();

        while (start < length) {
            int end = Math.min(start + chunkSize, length);

            // chunk 중간에서 단어가 끊기지 않도록 마지막 공백 위치까지만 자르기 시도
            if (end < length) {
                int lastSpace = text.lastIndexOf(' ', end);
                if (lastSpace > start) {
                    end = lastSpace;
                }
            }

            String piece = text.substring(start, end).trim();
            if (!piece.isEmpty()) {
                chunks.add(new TextChunk(piece, source, page));
            }

            if (end >= length) break;
            start = Math.max(end - chunkOverlap, start + 1); // overlap만큼 겹쳐서 다음 chunk 시작
        }
        return chunks;
    }
}
