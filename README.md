# RAG 기술을 활용한 데이터시트 분석 및 Q&A 챗봇 (Spring Boot 버전)

진로탐색학점제 참여 프로젝트. MCU/센서/통신모듈 영문 데이터시트를 업로드하면
자연어로 질문하고, 출처(문서명+페이지)가 명시된 한국어 답변을 받는 시스템.

> 계획서상 Python/FastAPI/LangChain 스택에서 **Spring Boot(Java)** 스택으로 변경.
> LangChain이 없으므로 OpenAI API와 Chroma Vector DB를 모두 REST API로 직접 호출한다.

## 팀 구성

| 이름 | 역할 |
|---|---|
| 박지호 | 시스템 아키텍처 설계, UI 인터페이스 개발, 모듈 통합 |
| 이헌영 | OpenAI API 연동, 프롬프트 엔지니어링, 답변 정확도 최적화 |
| 신승민 | PDF 데이터 전처리, 텍스트 분할 전략, 벡터 DB 구축/관리 |

## 기술 스택

- **Backend**: Java 17, Spring Boot 3.3, Spring Web, Spring Data JPA, Maven
- **DB**: H2 (개발용 파일 DB, 문서 메타데이터 + 채팅 이력)
- **Vector DB**: Chroma (별도 Python 프로세스로 실행, REST API로 연동)
- **PDF 처리**: Apache PDFBox
- **Frontend**: React, TypeScript
- **LLM**: OpenAI API (gpt-4o-mini, text-embedding-3-small) — REST API 직접 호출

## 왜 LangChain 없이 직접 호출하는가

LangChain은 Python 라이브러리라서 Spring Boot(Java)에서는 사용할 수 없다.
대신 각 역할을 다음으로 대체했다:

| LangChain 컴포넌트 | 이 프로젝트에서의 대응 |
|---|---|
| PyPDFLoader | `PdfExtractionService` (Apache PDFBox) |
| RecursiveCharacterTextSplitter | `TextSplitterService` (직접 구현) |
| OpenAIEmbeddings | `OpenAiEmbeddingClient` (RestClient로 `/v1/embeddings` 직접 호출) |
| Chroma 통합 | `ChromaClient` (RestClient로 Chroma v2 REST API 직접 호출) |
| ChatOpenAI | `OpenAiChatClient` (RestClient로 `/v1/chat/completions` 직접 호출) |
| RetrievalQA Chain | `RagService` (검색 → 프롬프트 구성 → LLM 호출을 직접 조립) |

## 폴더 구조

```
backend/
  src/main/java/com/team/datasheetrag/
    DatasheetRagApplication.java   # 진입점
    config/
      AppProperties.java           # application.yml 바인딩
      WebConfig.java                # CORS, RestClient Bean
    client/
      OpenAiEmbeddingClient.java   # [이헌영] OpenAI 임베딩 API 호출
      OpenAiChatClient.java         # [이헌영] OpenAI Chat API 호출
      ChromaClient.java             # [신승민] Chroma REST API 호출
    service/
      PdfExtractionService.java    # [신승민] PDF → 텍스트 추출
      TextSplitterService.java     # [신승민] Chunk 분리
      PromptService.java            # [이헌영] 시스템 프롬프트 구성
      RagService.java               # [이헌영] 검색+생성 통합
      DocumentService.java          # [박지호] 업로드 파이프라인 조율
      ChatHistoryService.java       # 채팅 이력 저장
    controller/
      DocumentController.java      # POST /api/documents/upload, GET /api/documents
      ChatController.java           # POST /api/chat
      HealthController.java
      GlobalExceptionHandler.java
    entity/
      Document.java, ChatMessage.java   # JPA 엔티티
    repository/
      DocumentRepository.java, ChatMessageRepository.java
    dto/
      ChatDtos.java, SourceItem.java, DocumentResponse.java, TextChunk.java
  src/main/resources/application.yml
  pom.xml

frontend/
  src/
    components/
      UploadZone.tsx       # 드래그앤드롭 업로드
      DocumentGrid.tsx     # 문서 목록
      ChatWindow.tsx       # 대화창
      SourceBadge.tsx      # 출처 표시
    api/client.ts          # 백엔드 통신 (포트 8080)
```

## 실행 방법 (인텔리제이, 대학생 라이센스 Ultimate 기준)

### 0. 사전 준비: Chroma 서버 (Python, 별도 프로세스)

Chroma 자체는 Python 패키지이며, Spring Boot 앱과는 별개의 프로세스로 떠 있어야 한다.
팀원 중 1명의 PC(또는 서버)에서 아래처럼 실행해두면 된다.

```bash
pip install chromadb
chroma run --path ./chroma-data --port 8000
```

→ `http://localhost:8000` 에서 Chroma 서버가 떠 있어야 Spring Boot 쪽 `ChromaClient`가 정상 동작한다.
`application.yml`의 `app.chroma.base-url` 값을 이 주소로 맞춰둘 것.

### 1. 백엔드 (Spring Boot)

1. 인텔리제이에서 `backend/pom.xml`을 **Open as Project** (또는 기존 프로젝트에 모듈로 추가)
2. 처음 열면 Maven이 자동으로 의존성을 다운로드한다 (Spring Boot, PDFBox, H2 등). 인터넷 연결 필요.
3. **환경변수 설정** — OpenAI API 키는 코드에 직접 넣지 말고 Run Configuration에 등록:
   - Run → Edit Configurations → `DatasheetRagApplication`
   - Environment variables: `OPENAI_API_KEY=sk-...`
4. `DatasheetRagApplication.java`의 ▶ 버튼으로 실행 (기본 포트 8080)
5. 브라우저에서 `http://localhost:8080` 접속 시 `{"status":"ok", ...}` 확인
6. H2 콘솔(데이터 확인용): `http://localhost:8080/h2-console`
   - JDBC URL: `jdbc:h2:file:./data/app`

### 2. 프론트엔드 (React)

```bash
cd frontend
npm install
npm start
```

`http://localhost:3000` 에서 화면 확인.

## 추진 일정 대응 현황 (현재 스캐폴딩 기준)

| 주차 | 계획 내용 | 코드 위치 |
|---|---|---|
| 3주차 | 개발환경 구축 (Python→Java로 변경) | `pom.xml`, Maven 프로젝트 구조 |
| 4주차 | OpenAI API 연동 | `OpenAiChatClient`, `OpenAiEmbeddingClient` |
| 5주차 | PDF 텍스트 추출 | `PdfExtractionService` (PDFBox) |
| 6주차 | 전처리, Chunk 분리 | `TextSplitterService` |
| 7주차 | Chroma Vector DB 저장 | `ChromaClient.addChunks()` |
| 8주차 | 유사도 검색 | `ChromaClient.query()` |
| 9주차 | RAG 통합 체인 | `RagService` |
| 10주차 | 프롬프트 최적화, 출처 페이지 표시 | `PromptService` |
| 11주차 | API 서버, DB 연동 | `controller/`, `entity/`, `repository/` |
| 12주차 | React 웹 UI | `frontend/` |

## 다음 단계 (TODO)

- [ ] Chroma 서버를 팀 공용으로 어디에 띄울지 결정 (개인 PC vs 학교 서버 vs 클라우드)
- [ ] 실제 데이터시트 샘플로 `TextSplitterService`의 chunkSize/overlap 튜닝
- [ ] 채팅 스트리밍 응답 (`ChatController`에 SSE 적용, `ChatWindow.tsx` 대응)
- [ ] 문서 삭제 API (Chroma + H2 동시 삭제)
- [ ] OPENAI_API_KEY를 인텔리제이 EnvFile 플러그인이나 `.env` 파일로 관리 (커밋 금지)
- [ ] 다중 제조사 데이터시트 교차 검증 테스트 (14주차)
- [ ] PDFBox로 표/레지스터 맵 추출 품질이 부족하면 영역 기반 추출(Rectangle) 추가 검토
- [ ] H2 → MySQL/PostgreSQL 전환 시 `application.yml`의 datasource만 교체하면 되도록 JPA 표준 문법 유지 확인
