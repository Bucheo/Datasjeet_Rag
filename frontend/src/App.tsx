import React, { useState } from "react";
import UploadZone from "./components/UploadZone";
import DocumentGrid from "./components/DocumentGrid";
import ChatWindow from "./components/ChatWindow";

/**
 * 계획서 UI/UX 요구사항을 그대로 레이아웃으로 옮긴 메인 화면:
 * "업로드 영역, 파싱이 완료된 기술 문서 목록 그리드, 답변이 실시간으로 출력되는
 *  스트리밍 대화창을 유기적으로 배치"
 */
export default function App() {
  const [refreshKey, setRefreshKey] = useState(0);
  const [selectedDocument, setSelectedDocument] = useState<string | null>(null);

  const handleUploadComplete = () => setRefreshKey((k) => k + 1);

  return (
    <div style={{ display: "grid", gridTemplateColumns: "320px 1fr", height: "100vh" }}>
      <aside style={{ borderRight: "1px solid #eee", padding: "1.5rem", overflowY: "auto" }}>
        <h2>📚 Datasheet RAG</h2>
        <UploadZone onUploadComplete={handleUploadComplete} />
        <div style={{ marginTop: "1.5rem" }}>
          <DocumentGrid
            refreshKey={refreshKey}
            selectedDocument={selectedDocument}
            onSelectDocument={setSelectedDocument}
          />
        </div>
      </aside>

      <main style={{ padding: "1.5rem" }}>
        <ChatWindow selectedDocument={selectedDocument} />
      </main>
    </div>
  );
}
