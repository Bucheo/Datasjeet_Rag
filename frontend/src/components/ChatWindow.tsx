import React, { useState } from "react";
import { askQuestion, SourceItem } from "../api/client";
import SourceBadge from "./SourceBadge";

interface Message {
  role: "user" | "assistant";
  content: string;
  sources?: SourceItem[];
}

interface ChatWindowProps {
  selectedDocument: string | null;
}

/**
 * 계획서 UI/UX 요구사항: "답변이 실시간으로 출력되는 스트리밍 대화창"
 * 현재 버전은 백엔드가 동기 응답을 주므로 일반 대화창으로 구현.
 * TODO(이헌영/박지호): 백엔드 SSE 스트리밍 전환 시 fetch + ReadableStream으로 교체.
 */
export default function ChatWindow({ selectedDocument }: ChatWindowProps) {
  const [messages, setMessages] = useState<Message[]>([]);
  const [input, setInput] = useState("");
  const [isLoading, setIsLoading] = useState(false);

  const handleSend = async () => {
    const question = input.trim();
    if (!question || isLoading) return;

    setMessages((prev) => [...prev, { role: "user", content: question }]);
    setInput("");
    setIsLoading(true);

    try {
      const result = await askQuestion(question, selectedDocument);
      setMessages((prev) => [
        ...prev,
        { role: "assistant", content: result.answer, sources: result.sources },
      ]);
    } catch (err) {
      setMessages((prev) => [
        ...prev,
        { role: "assistant", content: "답변 생성 중 오류가 발생했습니다." },
      ]);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div style={{ display: "flex", flexDirection: "column", height: "100%" }}>
      {selectedDocument && (
        <div style={{ fontSize: "0.8rem", color: "#666", marginBottom: "0.5rem" }}>
          현재 범위: <strong>{selectedDocument}</strong>
        </div>
      )}

      <div style={{ flex: 1, overflowY: "auto", marginBottom: "1rem" }}>
        {messages.map((msg, idx) => (
          <div
            key={idx}
            style={{
              textAlign: msg.role === "user" ? "right" : "left",
              margin: "0.5rem 0",
            }}
          >
            <div
              style={{
                display: "inline-block",
                background: msg.role === "user" ? "#2563eb" : "#f1f5f9",
                color: msg.role === "user" ? "#fff" : "#111",
                borderRadius: 8,
                padding: "0.6rem 0.9rem",
                maxWidth: "75%",
                textAlign: "left",
              }}
            >
              {msg.content}
            </div>
            {msg.sources && msg.sources.length > 0 && (
              <div>
                {msg.sources.map((s, i) => (
                  <SourceBadge key={i} source={s} />
                ))}
              </div>
            )}
          </div>
        ))}
        {isLoading && <p style={{ color: "#999" }}>답변 생성 중...</p>}
      </div>

      <div style={{ display: "flex", gap: "0.5rem" }}>
        <input
          value={input}
          onChange={(e) => setInput(e.target.value)}
          onKeyDown={(e) => e.key === "Enter" && handleSend()}
          placeholder="데이터시트에 대해 질문하세요 (예: GPIO 핀 동작 전압은?)"
          style={{ flex: 1, padding: "0.6rem", borderRadius: 6, border: "1px solid #ccc" }}
        />
        <button onClick={handleSend} disabled={isLoading} style={{ padding: "0.6rem 1.2rem" }}>
          전송
        </button>
      </div>
    </div>
  );
}
