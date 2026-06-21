import React, { useCallback, useState } from "react";
import { useDropzone } from "react-dropzone";
import { uploadDatasheet } from "../api/client";

interface UploadZoneProps {
  onUploadComplete: () => void;
}

/**
 * 계획서 UI/UX 요구사항:
 * "PDF 기술 문서를 마우스로 끌어다 놓는 업로드 영역"
 * "시스템 에러 및 데이터 처리 중임을 알리는 로딩 상태를 직관적으로 시각화"
 */
export default function UploadZone({ onUploadComplete }: UploadZoneProps) {
  const [status, setStatus] = useState<"idle" | "uploading" | "error">("idle");
  const [errorMessage, setErrorMessage] = useState("");

  const onDrop = useCallback(
    async (acceptedFiles: File[]) => {
      if (acceptedFiles.length === 0) return;
      setStatus("uploading");
      setErrorMessage("");
      try {
        // 여러 개 업로드 시 순차 처리 (벡터 DB 쓰기 충돌 방지)
        for (const file of acceptedFiles) {
          await uploadDatasheet(file);
        }
        setStatus("idle");
        onUploadComplete();
      } catch (err) {
        setStatus("error");
        setErrorMessage("업로드 중 오류가 발생했습니다. 다시 시도해주세요.");
      }
    },
    [onUploadComplete]
  );

  const { getRootProps, getInputProps, isDragActive } = useDropzone({
    onDrop,
    accept: { "application/pdf": [".pdf"] },
    multiple: true,
  });

  return (
    <div
      {...getRootProps()}
      style={{
        border: "2px dashed #999",
        borderRadius: 8,
        padding: "2rem",
        textAlign: "center",
        cursor: "pointer",
        background: isDragActive ? "#f0f7ff" : "#fafafa",
      }}
    >
      <input {...getInputProps()} />
      {status === "uploading" ? (
        <p>데이터시트 분석 중입니다... (텍스트 추출 → 벡터화 진행 중)</p>
      ) : isDragActive ? (
        <p>여기에 놓아주세요</p>
      ) : (
        <p>데이터시트 PDF를 드래그하거나 클릭하여 업로드하세요</p>
      )}
      {status === "error" && <p style={{ color: "red" }}>{errorMessage}</p>}
    </div>
  );
}
