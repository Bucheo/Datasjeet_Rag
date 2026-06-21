package com.team.datasheetrag.dto;

import com.team.datasheetrag.entity.Document;

import java.time.LocalDateTime;

public record DocumentResponse(
        String id,
        String filename,
        Integer pageCount,
        String status,
        LocalDateTime uploadedAt
) {
    public static DocumentResponse from(Document doc) {
        return new DocumentResponse(
                doc.getId(),
                doc.getFilename(),
                doc.getPageCount(),
                doc.getStatus(),
                doc.getUploadedAt()
        );
    }
}
