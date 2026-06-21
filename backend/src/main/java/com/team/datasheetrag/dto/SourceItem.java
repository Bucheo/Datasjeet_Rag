package com.team.datasheetrag.dto;

/** 답변의 근거가 된 문서명 + 페이지 번호. */
public record SourceItem(String filename, Integer page) {
}
