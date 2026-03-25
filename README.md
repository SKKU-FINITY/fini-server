# FINI Server 💳

금융 상품 데이터를 수집·저장·가공하여 예금·적금 비교 서비스에 활용할 수 있도록 만든 백엔드 서버입니다.  
금융감독원 Open API를 기반으로 상품 및 은행 정보를 관리하고, 인증, 문서화, 스케줄링, 컨테이너 실행 환경을 개발했습니다.

<br />

## 1. Project Overview 📌

- **프로젝트명**: FINI Server
- **개발 기간**: `2025.09 ~ 2025.12`
- **개발 인원**: `1명`
- **담당 역할**: `백엔드 개발 / 데이터 연동 / 서버 설정 / 배치 작업 구성`


### 개발 목적
금융 상품 정보는 기관별로 흩어져 있어 사용자가 예금·적금 상품을 비교하기 번거롭습니다.  
FINI Server는 금융 상품 데이터를 한곳에서 수집하고, 서비스에서 바로 활용할 수 있는 형태로 제공하기 위해 개발했습니다.

### 목표
- 금융감독원 API 기반의 예금·적금·은행 데이터 수집
- 서버 내부에서 상품 데이터를 일관된 구조로 관리
- JWT 기반 인증을 포함한 API 서버 구성
- 스케줄링과 외부 API 연동을 고려한 확장 가능한 구조 설계
- Docker 기반 실행 환경 구성

---

## 2. Tech Stack 🛠️

### Backend
- Java 17
- Spring Boot 3.5.6
- Spring Data JPA
- Spring Security
- JWT
- Spring Validation
- Spring Web

### Database / Cache
- MySQL

### API / Docs
- Springdoc OpenAPI Swagger UI
- 금융감독원 Open API
- Naver API
- Gemini API

### Infra / Deployment
- Docker
- Eclipse Temurin 17 JDK

### Build Tool
- Gradle

---

## 3. Key Features ✨

### 1) 금융 상품 데이터 연동
- 금융감독원 Open API를 통해 예금, 적금, 은행 데이터를 조회할 수 있도록 구성했습니다.
- 외부 API 정보를 환경 변수 기반으로 주입해 운영 환경에 맞게 분리했습니다.

### 2) 인증 및 보안
- Spring Security와 JWT 기반 인증 구조를 적용했습니다.
- 민감한 설정값은 코드에 직접 작성하지 않고 환경 변수로 관리하도록 구성했습니다.

### 3) 데이터 저장 및 처리
- Spring Data JPA와 MySQL을 활용해 금융 상품 데이터를 저장하고 조회할 수 있도록 구현했습니다.
- batch fetch 옵션을 적용해 데이터 처리 성능과 조회 구조를 고려했습니다.

### 4) 스케줄링 기반 확장
- 애플리케이션 레벨에서 Scheduling을 활성화해 정기적인 데이터 처리 작업을 붙일 수 있도록 구성했습니다.

### 5) API 문서화
- Swagger UI를 적용해 API를 문서화하고, 테스트 및 협업 시 바로 확인할 수 있도록 구성했습니다.

### 6) 컨테이너 실행 환경
- Dockerfile을 작성해 빌드된 JAR 파일을 컨테이너에서 실행할 수 있도록 구성했습니다.
- Asia/Seoul 타임존을 적용해 서버 실행 환경을 정리했습니다.

---

## 4. Architecture 🏗️

<img width="1106" height="727" alt="fini아키텍처" src="https://github.com/user-attachments/assets/ae6eb36f-028c-4895-9c0e-f3052e70d387" />

