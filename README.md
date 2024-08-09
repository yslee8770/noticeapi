# Notice API

## 프로젝트 개요

Notice API는 공지사항을 등록, 수정, 삭제, 조회할 수 있는 RESTful API를 제공하는 시스템입니다. 이 프로젝트는 대용량 트래픽을 고려하여 비동기 파일 처리와 캐싱 전략을 활용하여 성능을 최적화하였습니다. 또한, 마스터-슬레이브 구조의 데이터베이스 설정을 통해 읽기 작업과 쓰기 작업을 효율적으로 분리하였습니다.

## 기술 스택

- **언어**: Java
- **프레임워크**: Spring Boot
- **Persistence 프레임워크**: Hibernate
- **데이터베이스**: MySQL (Master-Slave 구조)
- **캐싱**: Redis
- **파일 처리**: 비동기 처리

## 엔드포인트

### NoticeController

#### 1. 공지사항 등록 (Create Notice)
- **Endpoint**: `POST /api/notices`
- **Request Parameters**:
  - `title` (String, required): 공지 제목
  - `content` (String, required): 공지 내용
  - `startDate` (LocalDateTime, required): 공지 시작 일시
  - `endDate` (LocalDateTime, required): 공지 종료 일시
  - `author` (String, required): 작성자
  - `files` (List<MultipartFile>, optional): 첨부파일 (여러 개)
- **Response**:
  - `id` (Long): 공지사항 ID
  - `title` (String): 공지 제목
  - `content` (String): 공지 내용
  - `createdDate` (LocalDateTime): 공지 등록 일시
  - `startDate` (LocalDateTime): 공지 시작 일시
  - `endDate` (LocalDateTime): 공지 종료 일시
  - `author` (String): 작성자
  - `files` (List<FileDto>): 첨부파일 목록

#### 2. 공지사항 수정 (Update Notice)
- **Endpoint**: `PUT /api/notices/{noticeId}`
- **Request Parameters**:
  - `title` (String, optional): 공지 제목
  - `content` (String, optional): 공지 내용
  - `startDate` (LocalDateTime, optional): 공지 시작 일시
  - `endDate` (LocalDateTime, optional): 공지 종료 일시
  - `author` (String, optional): 작성자
  - `files` (List<MultipartFile>, optional): 첨부파일 (여러 개)
- **Response**:
  - `id` (Long): 공지사항 ID
  - `title` (String): 공지 제목
  - `content` (String): 공지 내용
  - `createdDate` (LocalDateTime): 공지 등록 일시
  - `startDate` (LocalDateTime): 공지 시작 일시
  - `endDate` (LocalDateTime): 공지 종료 일시
  - `author` (String): 작성자
  - `files` (List<FileDto>): 첨부파일 목록

#### 3. 공지사항 삭제 (Delete Notice)
- **Endpoint**: `DELETE /api/notices/{noticeId}`
- **Request Parameters**: 없음
- **Response**:
  - `message` (String): "Notice deleted successfully."

#### 4. 공지사항 단일 조회 (Get Notice)
- **Endpoint**: `GET /api/notices/{noticeId}`
- **Request Parameters**: 없음
- **Response**:
  - `id` (Long): 공지사항 ID
  - `title` (String): 공지 제목
  - `content` (String): 공지 내용
  - `createdDate` (LocalDateTime): 공지 등록 일시
  - `views` (int): 조회수
  - `author` (String): 작성자
  - `files` (List<FileDto>): 첨부파일 목록

#### 5. 공지사항 전체 조회 (Get All Notices)
- **Endpoint**: `GET /api/notices`
- **Request Parameters**:
  - `page` (int, optional): 페이지 번호 (기본값: 0)
  - `size` (int, optional): 페이지당 항목 수 (기본값: 10)
- **Response**:
  - `notices` (List<NoticeResponseDto>): 공지사항 목록
    - `id` (Long): 공지사항 ID
    - `title` (String): 공지 제목
    - `content` (String): 공지 내용
    - `createdDate` (LocalDateTime): 공지 등록 일시
    - `views` (int): 조회수
    - `author` (String): 작성자
  - `totalPages` (int): 전체 페이지 수
  - `totalElements` (int): 전체 항목 수
  - `currentPage` (int): 현재 페이지 번호

### FileDownloadController

#### 1. 파일 다운로드 (Download File)
- **Endpoint**: `GET /api/files/{fileId}`
- **Request Parameters**: 없음
- **Response**: 파일 리소스 (바이너리 데이터)
  - **Headers**:
    - `Content-Disposition`: `attachment; filename="{originalFileName}"`
    - `Content-Type`: 파일의 MIME 타입

## 대용량 트래픽 고려사항

### 비동기 파일 처리
- 공지사항 등록 시 파일 업로드는 비동기적으로 처리되어 서버의 응답 시간을 단축하고 스레드를 효율적으로 사용합니다.
- `CompletableFuture`를 사용하여 파일 처리를 비동기적으로 수행합니다.

### Redis 캐싱
- 공지사항 조회 시 조회수는 Redis에 캐싱되어 데이터베이스에 대한 직접적인 부하를 줄입니다.
- 조회될 때마다 조회수 정보는 Redis에 추가되며, 캐싱을 통해 데이터베이스에 불필요한 조회 요청을 줄일 수 있습니다.
- 수정,삭제,등록시 캐싱데이터를 초기화하여 데이터의 정합성을 보존합니다.
- 공지사항은 수정이나 등록이 빈번하지 않으며, 조회가 많을 것으로 예상되어 Redis 캐싱을 사용하였습니다.

### 마스터-슬레이브 DB 구조
- 쓰기 작업(공지사항 등록, 수정, 삭제)은 마스터 DB에서 처리되며, 읽기 작업(공지사항 조회, 파일 조회)은 슬레이브 DB에서 처리하여 데이터베이스 부하를 분산합니다.
- 슬레이브 DB는 마스터 DB를 복제한 데이터베이스로, 실시간으로 동기화가 이루어진다는 가정 하에 설계되었습니다.


## 테스트

- 단위 테스트와 통합 테스트를 통해 각 기능의 정확성과 성능을 검증하였습니다.
- 대용량 트래픽 시나리오를 고려한 성능 테스트를 추가적으로 수행하였습니다.


