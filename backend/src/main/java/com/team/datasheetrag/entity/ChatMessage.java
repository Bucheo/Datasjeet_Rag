package com.team.datasheetrag.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/** 질문/답변 1턴. sourcesJson에는 출처(문서명+페이지) 목록을 JSON 문자열로 저장한다. */
@Entity
@Table(name = "chat_messages")
@Getter
@Setter
@NoArgsConstructor
public class ChatMessage {

    @Id
    private String id = UUID.randomUUID().toString();

    /** 특정 문서로 검색 범위를 좁힌 질문인 경우 해당 파일명, 아니면 null */
    private String documentFilter;

    /** "user" | "assistant" */
    @Column(nullable = false)
    private String role;

    @Lob
    @Column(nullable = false)
    private String content;

    @Lob
    private String sourcesJson;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
