# Design Document

## Overview

ECS 환경에서 발생하는 502, 503, 504 게이트웨이 에러를 해결하기 위한 설계입니다. 문제의 근본 원인은 ALB 헬스체크 설정, 컨테이너 리소스 부족, 애플리케이션 시작 시간, Dockerfile 설정 오류 등 여러 요인이 복합적으로 작용하고 있습니다.

## Architecture

### 현재 문제점 분석

1. **ALB Target Group 설정 불일치**
   - 타겟 그룹 포트: 8080
   - 헬스체크 경로: `/` (Spring Boot 기본 경로는 404 반환 가능)
   - 헬스체크 타임아웃: 10초 (기본값, 너무 짧을 수 있음)
   - Deregistration delay: 30초 (진행 중인 요청 처리에 부족할 수 있음)

2. **ECS Task Definition 리소스 부족**
   - 메모리: 512MB (Spring Boot + JVM에 부족)
   - CPU: 512 (0.5 vCPU, 부족할 수 있음)
   - JVM 힙 메모리 설정 없음 (기본값 사용 시 OOM 위험)

3. **Dockerfile 오류**
   - ENTRYPOINT: `/app.jar` → 실제 파일은 `app.jar` (루트 경로 오류)

4. **ECS Service 설정**
   - Health check grace period: 60초 (Spring Boot 시작 시간 고려 시 부족)
   - 데이터베이스 연결, 외부 API 초기화 시간 미고려

5. **보안 그룹 및 네트워크**
   - ALB → ECS 태스크 통신 확인 필요
   - ECS 태스크 → RDS 통신 확인 필요

## Components and Interfaces

### 1. Dockerfile 수정

**변경 사항:**
- ENTRYPOINT 경로 수정: `/app.jar` → `app.jar`
- JVM 메모리 설정 추가: `-Xmx` 옵션으로 힙 메모리 제한
- 컨테이너 메모리의 75% 정도를 JVM에 할당 (나머지는 네이티브 메모리용)

```dockerfile
FROM eclipse-temurin:17-jdk
ENV TZ=Asia/Seoul
RUN ln -snf /usr/share/zoneinfo/Asia/Seoul /etc/localtime && echo "Asia/Seoul" > /etc/timezone
ARG JAR_FILE=build/libs/fini-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java", "-Xmx768m", "-Xms768m", "-jar", "app.jar"]
```

### 2. ECS Task Definition 수정

**변경 사항:**
- 메모리: 512MB → 1024MB (1GB)
- CPU: 512 → 1024 (1 vCPU)
- 컨테이너 메모리도 동일하게 증가

**이유:**
- Spring Boot 애플리케이션은 최소 1GB 메모리 권장
- JVM 힙 + 네이티브 메모리 + 메타스페이스 고려
- 데이터베이스 연결 풀, 외부 API 클라이언트 등의 메모리 사용

### 3. ALB Target Group 헬스체크 수정

**변경 사항:**
- 헬스체크 경로: `/` → `/actuator/health` (Spring Boot Actuator 사용)
- 대안: `/dashboard` (현재 사용 중인 엔드포인트)
- Healthy threshold: 2 (기본값 유지 또는 감소)
- Unhealthy threshold: 3 (증가)
- Interval: 30초 → 15초 (더 빠른 감지)
- Timeout: 10초 유지
- Matcher: `200-499` → `200` (정확한 성공 응답만 허용)

**헬스체크 엔드포인트 선택:**
- Option 1: Spring Boot Actuator 추가 (`/actuator/health`)
  - 장점: 표준화된 헬스체크, 데이터베이스 연결 상태 포함
  - 단점: 의존성 추가 필요
- Option 2: 기존 `/dashboard` 사용
  - 장점: 추가 작업 없음
  - 단점: 실제 헬스 상태를 정확히 반영하지 못할 수 있음
- **권장: Option 1 (Actuator 추가)**

### 4. ECS Service 수정

**변경 사항:**
- Health check grace period: 60초 → 180초 (3분)
- Deregistration delay: 30초 → 60초

**이유:**
- Spring Boot 애플리케이션 시작 시간: 30-60초
- 데이터베이스 연결 초기화: 10-20초
- 외부 API 초기화 (FSS, Naver, Gemini): 10-20초
- 여유 시간 포함하여 180초 설정

### 5. Spring Boot Actuator 추가

**build.gradle 수정:**
```gradle
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-actuator'
    // ... 기존 의존성
}
```

**application.yaml 수정:**
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health
      base-path: /actuator
  endpoint:
    health:
      show-details: always
  health:
    db:
      enabled: true
```

### 6. 보안 그룹 규칙 확인

**확인 사항:**
- ALB 보안 그룹 → ECS 태스크 보안 그룹: 8080 포트 허용
- ECS 태스크 보안 그룹 → RDS 보안 그룹: 3306 포트 허용
- ECS 태스크 보안 그룹 아웃바운드: 모든 트래픽 허용 (외부 API 호출용)

## Data Models

변경 사항 없음 (인프라 설정 변경만 해당)

## Error Handling

### 1. 컨테이너 시작 실패

**증상:**
- ECS 태스크가 반복적으로 시작/중지
- CloudWatch Logs에 OOM 에러 또는 시작 실패 로그

**해결:**
- 메모리 증가 (1024MB)
- JVM 힙 메모리 명시적 설정
- CloudWatch Logs 확인

### 2. 헬스체크 실패

**증상:**
- ALB 타겟 그룹에서 타겟이 unhealthy 상태
- 502/503 에러 발생

**해결:**
- 헬스체크 경로를 유효한 엔드포인트로 변경
- Grace period 증가
- 애플리케이션 로그에서 시작 시간 확인

### 3. 데이터베이스 연결 실패

**증상:**
- 애플리케이스 시작 후 데이터베이스 연결 에러
- 504 Gateway Timeout

**해결:**
- 보안 그룹 규칙 확인
- RDS 엔드포인트 및 자격 증명 확인
- HikariCP 연결 풀 설정 확인

### 4. 배포 중 다운타임

**증상:**
- 새 태스크 배포 시 일시적인 503 에러

**해결:**
- Deregistration delay 증가 (60초)
- Health check grace period 적절히 설정
- 롤링 업데이트 설정 확인 (200% max, 100% min)

## Testing Strategy

### 1. 로컬 Docker 테스트

```bash
# 이미지 빌드
./gradlew clean build
docker build -t fini-app:test .

# 로컬 실행 (환경 변수 주입)
docker run -p 8080:8080 \
  -e DB_URL="jdbc:mysql://..." \
  -e DB_USERNAME="..." \
  -e DB_PASSWORD="..." \
  -e JWT_SECRET="..." \
  fini-app:test

# 헬스체크 테스트
curl http://localhost:8080/actuator/health
curl http://localhost:8080/dashboard
```

### 2. ECS 배포 후 검증

```bash
# ECS 태스크 상태 확인
aws ecs describe-tasks --cluster fini-cluster --tasks <task-id>

# CloudWatch Logs 확인
aws logs tail /ecs/fini-app-task --follow

# ALB 타겟 그룹 상태 확인
aws elbv2 describe-target-health --target-group-arn <arn>

# 헬스체크 테스트 (ALB DNS)
curl https://<alb-dns>/actuator/health
```

### 3. 부하 테스트

```bash
# Apache Bench로 간단한 부하 테스트
ab -n 1000 -c 10 https://<alb-dns>/dashboard

# 메모리 사용량 모니터링
aws cloudwatch get-metric-statistics \
  --namespace AWS/ECS \
  --metric-name MemoryUtilization \
  --dimensions Name=ServiceName,Value=fini-app-service \
  --start-time 2024-01-01T00:00:00Z \
  --end-time 2024-01-01T01:00:00Z \
  --period 300 \
  --statistics Average
```

### 4. 롤링 업데이트 테스트

1. 코드 변경 후 새 이미지 빌드 및 푸시
2. ECS 서비스 업데이트
3. 배포 중 지속적으로 헬스체크 수행
4. 에러 발생 여부 확인
5. CloudWatch Logs에서 이전 태스크 종료 시점 확인

## Implementation Notes

### 우선순위

1. **Critical (즉시 수정 필요):**
   - Dockerfile ENTRYPOINT 수정
   - ECS Task Definition 메모리/CPU 증가
   - ALB 타겟 그룹 헬스체크 경로 수정

2. **High (빠른 시일 내 수정):**
   - Spring Boot Actuator 추가
   - Health check grace period 증가
   - Deregistration delay 증가

3. **Medium (개선 사항):**
   - CloudWatch Logs 설정 최적화
   - 보안 그룹 규칙 검토 및 최소 권한 적용
   - 모니터링 대시보드 구성

### 배포 순서

1. 애플리케이션 코드 수정 (Actuator 추가)
2. Dockerfile 수정
3. 로컬 Docker 테스트
4. ECR에 새 이미지 푸시
5. Terraform 코드 수정 (ECS, ALB)
6. Terraform apply
7. ECS 서비스 업데이트 (새 태스크 정의 사용)
8. 헬스체크 및 모니터링

### 롤백 계획

- Terraform state 백업
- 이전 ECS 태스크 정의 보관
- 문제 발생 시 이전 태스크 정의로 즉시 롤백
- ALB 타겟 그룹을 EC2로 전환 가능하도록 유지
