package com.team.datasheetrag.service;

import com.team.datasheetrag.dto.TextChunk;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * [담당: 신승민 - PDF 데이터 전처리]
 *
 * 추진 일정 5주차: PDF Loader를 이용한 데이터시트 내부 영문 텍스트 추출.
 * 6주차: 오탈자 전처리는 cleanText()에서 처리.
 *
 * Apache PDFBox 사용 (Python의 PyPDFLoader에 대응하는 Java 라이브러리).
 */
@Service
public class PdfExtractionService {

    /** 페이지 단위로 텍스트를 추출한다. 각 결과의 page는 1부터 시작. */
    public List<TextChunk> extractByPage(File pdfFile) throws IOException {
        List<TextChunk> pages = new ArrayList<>();
        String filename = pdfFile.getName();

        try (PDDocument document = Loader.loadPDF(pdfFile)) {
            PDFTextStripper stripper = new PDFTextStripper();
            int totalPages = document.getNumberOfPages();

            for (int pageNum = 1; pageNum <= totalPages; pageNum++) {
                stripper.setStartPage(pageNum);
                stripper.setEndPage(pageNum);
                String rawText = stripper.getText(document);
                pages.add(new TextChunk(cleanText(rawText), filename, pageNum));
            }
        }
        return pages;
    }

    public int countPages(File pdfFile) throws IOException {
        try (PDDocument document = Loader.loadPDF(pdfFile)) {
            return document.getNumberOfPages();
        }
    }

    /**
     * 6주차 전처리 자리.
     * 데이터시트 PDF는 헤더/푸터 반복, 줄바꿈 깨짐 등이 흔하다.
     * TODO(신승민): 실제 샘플 데이터시트를 보고 정규식 규칙 보강
     */
    private String cleanText(String rawText) {
        if (rawText == null) return "";
        return rawText.replaceAll("\\s+", " ").trim();
    }
}
