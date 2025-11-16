# Fini 시스템 문서화 - 요구사항

## 소개

Fini는 금융감독원 API를 활용하여 예금 및 적금 상품 정보를 제공하고, 네이버 API와 Gemini AI를 통해 인기도 기반 추천 서비스를 제공하는 금융 상품 비교 플랫폼입니다. Spring Boot 기반의 RESTful API 서버로 구현되었으며, AWS 인프라 위에서 운영됩니다.

## 용어 정의

- **Fini System**: 금융 상품 비교 및 추천 플랫폼의 전체 시스템
- **FSS API**: 금융감독원(Financial Supervisory Service)에서 제공하는 금융 상품 정보 API
- **Naver DataLab API**: 네이버에서 제공하는 검색 트렌드 분석 API
- **Naver News API**: 네이버에서 제공하는 뉴스 검색 API
- **Gemini API**: Google의 생성형 AI API
- **JWT**: JSON Web Token, 사용자 인증을 위한 토큰 기반 인증 방식
- **Batch Service**: 정기적으로 실행되는 인기도 점수 계산 작업
- **Product Popularity**: 상품의 인기도 점수 및 AI 요약 정보
- **RAG**: Retrieval-Augmented Generation, 검색 증강 생성 기법

## 요구사항

### 요구사항 1: 사용자 인증 및 관리

**사용자 스토리:** 사용자로서, 회원가입과 로그인을 통해 시스템에 접근하고 내 정보를 관리할 수 있기를 원합니다.

#### 수락 기준

1. WHEN 사용자가 유효한 회원가입 정보를 제공하면, THE Fini System SHALL 새로운 사용자 계정을 생성한다
2. WHEN 사용자가 유효한 로그인 정보를 제공하면, THE Fini System SHALL JWT 토큰을 발급한다
3. WHEN 인증된 사용자가 내 정보 조회를 요청하면, THE Fini System SHALL 사용자의 프로필 정보를 반환한다
4. THE Fini System SHALL 비밀번호를 BCrypt 알고리즘으로 암호화하여 저장한다
5. THE Fini System SHALL JWT 토큰의 유효기간을 1시간으로 설정한다

### 요구사항 2: 금융 상품 데이터 동기화

**사용자 스토리:** 관리자로서, 금융감독원의 최신 상품 정보를 시스템에 동기화하여 사용자에게 정확한 정보를 제공하고 싶습니다.

#### 수락 기준

1. WHEN 관리자가 동기화 API를 호출하면, THE Fini System SHALL FSS API로부터 은행 정보를 조회하여 저장한다
2. WHEN 관리자가 적금 상품 동기화를 요청하면, THE Fini System SHALL FSS API로부터 적금 상품 및 옵션 정보를 조회하여 저장한다
3. WHEN 관리자가 예금 상품 동기화를 요청하면, THE Fini System SHALL FSS API로부터 예금 상품 및 옵션 정보를 조회하여 저장한다
4. THE Fini System SHALL 상품 정보를 은행 정보와 연관하여 저장한다
5. THE Fini System SHALL 중복된 상품 정보를 금융회사 코드와 상품 코드의 조합으로 식별하여 방지한다

### 요구사항 3: 금융 상품 조회 및 필터링

**사용자 스토리:** 사용자로서, 은행명과 저축 기간 등의 조건으로 예금 및 적금 상품을 필터링하여 조회하고 싶습니다.

#### 수락 기준

1. WHEN 사용자가 은행명 필터를 적용하면, THE Fini System SHALL 해당 은행의 상품만 반환한다
2. WHEN 사용자가 저축 기간 필터를 적용하면, THE Fini System SHALL 해당 기간의 옵션을 가진 상품만 반환한다
3. THE Fini System SHALL 조회 결과를 기본 금리 내림차순으로 정렬하여 반환한다
4. WHEN 사용자가 특정 상품의 상세 정보를 요청하면, THE Fini System SHALL 상품의 모든 옵션 정보를 포함하여 반환한다
5. WHEN 사용자가 특정 옵션 ID와 함께 상품 상세 정보를 요청하면, THE Fini System SHALL 해당 옵션 정보만 반환한다

### 요구사항 4: 인기도 기반 상품 추천

**사용자 스토리:** 사용자로서, 현재 인기 있는 금융 상품을 추천받아 트렌드에 맞는 상품을 선택하고 싶습니다.

#### 수락 기준

1. THE Fini System SHALL 매일 오후 8시 43분에 배치 작업을 실행하여 상품 인기도를 업데이트한다
2. WHEN 배치 작업이 실행되면, THE Fini System SHALL Naver News API를 호출하여 각 상품의 뉴스 언급 횟수를 수집한다
3. WHEN 배치 작업이 실행되면, THE Fini System SHALL Naver DataLab API를 호출하여 최근 30일간의 검색량을 수집한다
4. THE Fini System SHALL 검색량과 뉴스 언급 횟수를 가중 합산하여 최종 인기도 점수를 계산한다
5. WHEN 사용자가 인기 상품 목록을 요청하면, THE Fini System SHALL 인기도 점수 상위 5개 상품을 반환한다

### 요구사항 5: AI 기반 상품 요약 생성

**사용자 스토리:** 사용자로서, 각 상품이 왜 인기 있는지에 대한 AI 요약 정보를 제공받아 빠르게 이해하고 싶습니다.

#### 수락 기준

1. WHEN 배치 작업이 상품을 처리하면, THE Fini System SHALL 상품명, 우대조건, 최신 뉴스 정보를 포함한 프롬프트를 생성한다
2. WHEN 프롬프트가 생성되면, THE Fini System SHALL Gemini API를 호출하여 1-2줄의 추천 멘트를 생성한다
3. THE Fini System SHALL 생성된 AI 요약을 상품 인기도 정보와 함께 저장한다
4. WHEN 사용자가 인기 상품을 조회하면, THE Fini System SHALL AI 요약 정보를 포함하여 반환한다
5. IF Gemini API 호출이 실패하면, THEN THE Fini System SHALL 기본 메시지를 저장한다

### 요구사항 6: 상품 비교 추천

**사용자 스토리:** 사용자로서, 현재 보고 있는 상품과 비교할 수 있는 다른 인기 상품들을 추천받고 싶습니다.

#### 수락 기준

1. WHEN 사용자가 특정 상품의 비교 추천을 요청하면, THE Fini System SHALL 해당 상품을 제외한 동일 유형의 상품들을 조회한다
2. THE Fini System SHALL 비교 추천 상품들을 인기도 점수 내림차순으로 정렬하여 반환한다
3. THE Fini System SHALL 각 비교 상품의 인기도 점수와 AI 요약을 포함하여 반환한다
4. THE Fini System SHALL 적금 상품과 예금 상품을 별도로 비교 추천한다

### 요구사항 7: API 문서화 및 모니터링

**사용자 스토리:** 개발자로서, API 명세를 쉽게 확인하고 테스트할 수 있는 문서를 제공받고 싶습니다.

#### 수락 기준

1. THE Fini System SHALL Swagger UI를 통해 모든 API 엔드포인트를 문서화한다
2. THE Fini System SHALL 각 API의 요청/응답 스키마를 Swagger에 표시한다
3. THE Fini System SHALL Swagger UI에서 직접 API를 테스트할 수 있는 기능을 제공한다
4. THE Fini System SHALL 헬스 체크 엔드포인트를 제공한다

### 요구사항 8: 클라우드 인프라 구축

**사용자 스토리:** 운영자로서, 안정적이고 확장 가능한 클라우드 인프라 위에서 서비스를 운영하고 싶습니다.

#### 수락 기준

1. THE Fini System SHALL AWS ECS를 사용하여 컨테이너 기반으로 배포된다
2. THE Fini System SHALL AWS RDS MySQL을 데이터베이스로 사용한다
3. THE Fini System SHALL AWS ALB를 통해 트래픽을 분산한다
4. THE Fini System SHALL AWS CloudFront를 통해 CDN 서비스를 제공한다
5. THE Fini System SHALL AWS ECR에 Docker 이미지를 저장한다
6. THE Fini System SHALL AWS Secrets Manager를 통해 민감한 설정 정보를 관리한다
7. THE Fini System SHALL Terraform을 사용하여 인프라를 코드로 관리한다

### 요구사항 9: 데이터 무결성 및 성능

**사용자 스토리:** 시스템 관리자로서, 데이터의 무결성을 보장하고 높은 성능을 유지하고 싶습니다.

#### 수락 기준

1. THE Fini System SHALL JPA Auditing을 사용하여 엔티티의 생성 및 수정 시간을 자동으로 기록한다
2. THE Fini System SHALL Fetch Join을 사용하여 N+1 쿼리 문제를 방지한다
3. THE Fini System SHALL 배치 작업에서 페이징과 병렬 스트림을 사용하여 대량 데이터를 효율적으로 처리한다
4. THE Fini System SHALL 데이터베이스 연결 풀의 keepalive 시간을 5분으로 설정한다
5. THE Fini System SHALL 데이터베이스 연결의 최대 수명을 10분으로 설정한다

### 요구사항 10: 에러 처리 및 로깅

**사용자 스토리:** 개발자로서, 시스템 오류를 쉽게 추적하고 디버깅할 수 있는 로깅 시스템을 원합니다.

#### 수락 기준

1. THE Fini System SHALL 모든 API 응답을 통일된 ApiResponse 형식으로 반환한다
2. THE Fini System SHALL 예외 발생 시 적절한 에러 코드와 메시지를 반환한다
3. THE Fini System SHALL SLF4J를 사용하여 주요 작업의 로그를 기록한다
4. THE Fini System SHALL 배치 작업의 시작, 진행, 완료 상태를 로그로 기록한다
5. IF 외부 API 호출이 실패하면, THEN THE Fini System SHALL 에러 로그를 기록하고 적절한 예외를 발생시킨다
