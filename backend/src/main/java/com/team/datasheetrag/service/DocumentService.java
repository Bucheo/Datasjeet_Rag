package com.team.datasheetrag.service;

import com.team.datasheetrag.client.ChromaClient;
import com.team.datasheetrag.config.AppProperties;
import com.team.datasheetrag.dto.TextChunk;
import com.team.datasheetrag.entity.Document;
import com.team.datasheetrag.repository.DocumentRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * [담당: 박지호 - 모듈 통합]
 *
 * 업로드 → PDF 추출(신승민) → chunk 분리(신승민) → 벡터 DB 저장(신승민/이헌영 영역과 연결)
 * 까지 전체 파이프라인을 순서대로 호출하는 조율자.
 */
@Service
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final PdfExtractionService pdfExtractionService;
    private final TextSplitterService textSplitterService;
    private final ChromaClient chromaClient;
    private final String uploadDir;

    public DocumentService(DocumentRepository documentRepository,
                            PdfExtractionService pdfExtractionService,
                            TextSplitterService textSplitterService,
                            ChromaClient chromaClient,
                            AppProperties appProperties) {
        this.documentRepository = documentRepository;
        this.pdfExtractionService = pdfExtractionService;
        this.textSplitterService = textSplitterService;
        this.chromaClient = chromaClient;
        this.uploadDir = appProperties.upload().dir();
    }

    public Document uploadAndProcess(MultipartFile file) throws IOException {
        if (file.getOriginalFilename() == null || !file.getOriginalFilename().toLowerCase().endsWith(".pdf")) {
            throw new IllegalArgumentException("PDF 파일만 업로드할 수 있습니다.");
        }

        Path dir = Path.of(uploadDir);
        Files.createDirectories(dir);
        Path destPath = dir.resolve(file.getOriginalFilename());
        file.transferTo(destPath);

        Document document = new Document();
        document.setFilename(file.getOriginalFilename());
        document.setStatus("processing");
        documentRepository.save(document);

        try {
            File pdfFile = destPath.toFile();

            // 1. PDF 텍스트 추출 (신승민)
            List<TextChunk> pages = pdfExtractionService.extractByPage(pdfFile);
            // 2. Chunk 분리 (신승민)
            List<TextChunk> chunks = textSplitterService.split(pages);
            // 3. 임베딩 + 벡터 DB 저장 (신승민 영역, OpenAI 호출은 이헌영 영역과 연결)
            chromaClient.addChunks(chunks);

            document.setStatus("ready");
            document.setPageCount(pages.size());
        } catch (Exception e) {
            document.setStatus("failed");
            documentRepository.save(document);
            throw new RuntimeException("문서 처리 중 오류 발생: " + e.getMessage(), e);
        }

        documentRepository.save(document);
        return document;
    }

    public List<Document> listAll() {
        return documentRepository.findAllByOrderByUploadedAtDesc();
    }
}
