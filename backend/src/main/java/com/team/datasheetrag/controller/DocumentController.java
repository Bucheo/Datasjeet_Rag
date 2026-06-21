package com.team.datasheetrag.controller;

import com.team.datasheetrag.dto.DocumentResponse;
import com.team.datasheetrag.entity.Document;
import com.team.datasheetrag.service.DocumentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    /** PDF 업로드 → 전처리 → 벡터 DB 저장까지 전체 파이프라인 트리거. */
    @PostMapping("/upload")
    public ResponseEntity<DocumentResponse> upload(@RequestParam("file") MultipartFile file) {
        try {
            Document document = documentService.uploadAndProcess(file);
            return ResponseEntity.ok(DocumentResponse.from(document));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /** 업로드된 문서 목록 (DocumentGrid.tsx에서 사용). */
    @GetMapping
    public List<DocumentResponse> list() {
        return documentService.listAll().stream()
                .map(DocumentResponse::from)
                .toList();
    }
}
