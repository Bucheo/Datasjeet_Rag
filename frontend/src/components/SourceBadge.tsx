import React from "react";
import { SourceItem } from "../api/client";

/**
 * 계획서 핵심 요구사항: "정보가 발췌된 실제 문서명과 정확한 페이지 번호를 함께 명시"
 * 하드웨어 손상 방지를 위한 신뢰성 확보 장치이므로, 답변마다 눈에 잘 보이게 배치한다.
 */
export default function SourceBadge({ source }: { source: SourceItem }) {
  return (
    <span
      style={{
        display: "inline-block",
        background: "#eef2ff",
        color: "#3730a3",
        fontSize: "0.75rem",
        padding: "0.2rem 0.5rem",
        borderRadius: 12,
        marginRight: "0.4rem",
        marginTop: "0.3rem",
      }}
      title="이 답변의 출처"
    >
      📄 {source.filename}{source.page ? ` · p.${source.page}` : ""}
    </span>
  );
}
