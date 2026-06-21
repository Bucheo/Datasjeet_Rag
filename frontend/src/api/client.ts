import axios from "axios";

// Spring Boot 기본 포트는 8080 (application.yml의 server.port 참고)
const API_BASE_URL = process.env.REACT_APP_API_BASE_URL || "http://localhost:8080";

export const apiClient = axios.create({
  baseURL: API_BASE_URL,
});

export interface DocumentItem {
  id: string;
  filename: string;
  pageCount: number | null;
  status: "processing" | "ready" | "failed";
  uploadedAt: string;
}

export interface SourceItem {
  filename: string;
  page: number | null;
}

export interface ChatResponse {
  answer: string;
  sources: SourceItem[];
}

export async function uploadDatasheet(file: File): Promise<DocumentItem> {
  const formData = new FormData();
  formData.append("file", file);
  const { data } = await apiClient.post<DocumentItem>("/api/documents/upload", formData, {
    headers: { "Content-Type": "multipart/form-data" },
  });
  return data;
}

export async function fetchDocuments(): Promise<DocumentItem[]> {
  const { data } = await apiClient.get<DocumentItem[]>("/api/documents");
  return data;
}

export async function askQuestion(
  question: string,
  documentFilter?: string | null
): Promise<ChatResponse> {
  const { data } = await apiClient.post<ChatResponse>("/api/chat", {
    question,
    documentFilter: documentFilter ?? null,
  });
  return data;
}
