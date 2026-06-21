package com.team.datasheetrag.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/** 업로드된 데이터시트 PDF의 메타데이터. 실제 벡터 임베딩은 Chroma에 저장되고, 여긴 메타정보만 관리. */
@Entity
@Table(name = "documents")
@Getter
@Setter
@NoArgsConstructor
public class Document {

    @Id
    private String id = UUID.randomUUID().toString();

    @Column(nullable = false)
    private String filename;

    private Integer pageCount;

    /** processing | ready | failed */
    @Column(nullable = false)
    private String status = "processing";

    @Column(nullable = false)
    private LocalDateTime uploadedAt = LocalDateTime.now();
}
