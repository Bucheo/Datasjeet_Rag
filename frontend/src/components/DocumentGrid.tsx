import React, { useEffect, useState } from "react";
import { fetchDocuments, DocumentItem } from "../api/client";

interface DocumentGridProps {
  refreshKey: number; // 업로드 완료 시 부모에서 증가시켜 재조회 트리거
  selectedDocument: string | null;
  onSelectDocument: (filename: string | null) => void;
}

const statusLabel: Record<DocumentItem["status"], string> = {
  processing: "처리 중",
  ready: "분석 완료",
  failed: "처리 실패",
};

/** 계획서 UI/UX 요구사항: "파싱이 완료된 기술 문서 목록 그리드" */
export default function DocumentGrid({
  refreshKey,
  selectedDocument,
  onSelectDocument,
}: DocumentGridProps) {
  const [documents, setDocuments] = useState<DocumentItem[]>([]);

  useEffect(() => {
    fetchDocuments().then(setDocuments).catch(console.error);
  }, [refreshKey]);

  return (
    <div>
      <h3>업로드된 데이터시트</h3>
      <div
        style={{
          display: "grid",
          gridTemplateColumns: "repeat(auto-fill, minmax(180px, 1fr))",
          gap: "0.75rem",
        }}
      >
        {documents.map((doc) => (
          <div
            key={doc.id}
            onClick={() => onSelectDocument(doc.filename === selectedDocument ? null : doc.filename)}
            style={{
              border: doc.filename === selectedDocument ? "2px solid #2563eb" : "1px solid #ddd",
              borderRadius: 8,
              padding: "0.75rem",
              cursor: "pointer",
            }}
          >
            <strong style={{ fontSize: "0.85rem", wordBreak: "break-all" }}>
              {doc.filename}
            </strong>
            <p style={{ fontSize: "0.75rem", color: "#666", margin: "0.25rem 0 0" }}>
              {statusLabel[doc.status]}
              {doc.pageCount ? ` · ${doc.pageCount}p` : ""}
            </p>
          </div>
        ))}
        {documents.length === 0 && <p style={{ color: "#999" }}>아직 업로드된 문서가 없습니다.</p>}
      </div>
    </div>
  );
}
