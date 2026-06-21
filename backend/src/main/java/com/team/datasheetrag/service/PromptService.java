package com.team.datasheetrag.service;

import com.team.datasheetrag.dto.TextChunk;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * [담당: 이헌영 - 프롬프트 엔지니어링]
 *
 * 추진 일정 9~10주차: 답변 정확도 향상을 위한 프롬프트 최적화.
 *
 * 계획서 핵심 요구사항:
 *   "할루시네이션을 원천 차단하고 오직 주어진 기술 문서 안에서만 정답을 도출"
 *   "정보가 발췌된 실제 문서명과 정확한 페이지 번호를 함께 명시"
 * → 이 두 가지가 시스템 프롬프트의 핵심 제약.
 */
@Service
public class PromptService {

    private static final String SYSTEM_PROMPT_TEMPLATE = """
            당신은 임베디드 엔지니어를 위한 데이터시트 분석 어시스턴트입니다.

            규칙:
            1. 반드시 아래 [참고 문서] 안의 내용만 근거로 답변하세요. 문서에 없는 내용은 절대 추측하거나 지어내지 마세요. \
            답을 찾을 수 없으면 "제공된 문서에서 해당 정보를 찾을 수 없습니다"라고 답하세요.
            2. 레지스터 주소, 핀 번호, 전압/전류 수치 등 숫자 정보는 문서 원문과 정확히 일치해야 합니다. \
            하드웨어 손상으로 이어질 수 있으므로 단 한 글자도 추측하지 마세요.
            3. 답변은 한국어로, 간결하고 명확하게 작성하세요.
            4. 답변 마지막에 근거로 사용한 [문서명, 페이지]를 모두 표기하세요.

            [참고 문서]
            %s
            """;

    public String buildSystemPrompt(List<TextChunk> retrievedChunks) {
        String context = formatContext(retrievedChunks);
        return SYSTEM_PROMPT_TEMPLATE.formatted(context);
    }

    /** 검색된 chunk를 출처가 보이는 형태로 하나의 문자열로 합친다. */
    private String formatContext(List<TextChunk> chunks) {
        StringBuilder sb = new StringBuilder();
        for (TextChunk chunk : chunks) {
            sb.append("[").append(chunk.source()).append(", p.").append(chunk.page()).append("]\n");
            sb.append(chunk.content());
            sb.append("\n\n---\n\n");
        }
        return sb.toString();
    }
}
