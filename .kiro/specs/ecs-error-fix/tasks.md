# Implementation Plan

- [ ] 1. Dockerfile 수정 및 로컬 테스트
  - ENTRYPOINT 경로를 `/app.jar`에서 `app.jar`로 수정
  - JVM 메모리 옵션 추가 (`-Xmx768m -Xms768m`)
  - 로컬에서 Docker 이미지 빌드 및 실행 테스트
  - _Requirements: 2.4_

- [ ] 2. Spring Boot Actuator 추가 및 헬스체크 엔드포인트 구성
- [ ] 2.1 build.gradle에 Actuator 의존성 추가
  - `spring-boot-starter-actuator` 의존성 추가
  - _Requirements: 1.5_

- [ ] 2.2 application.yaml에 Actuator 설정 추가
  - `/actuator/health` 엔드포인트 노출 설정
  - 데이터베이스 헬스체크 활성화
  - _Requirements: 1.5, 3.4_

- [ ]* 2.3 로컬에서 헬스체크 엔드포인트 테스트
  - 애플리케이션 실행 후 `/actuator/health` 응답 확인
  - 데이터베이스 연결 상태가 헬스체크에 포함되는지 확인
  - _Requirements: 1.5_

- [ ] 3. ECS Task Definition 리소스 증가
- [ ] 3.1 terraform/modules/ecs/main.tf의 Task Definition 수정
  - CPU를 512에서 1024로 증가
  - 메모리를 512에서 1024로 증가
  - 컨테이너 정의의 cpu와 memory도 동일하게 수정
  - _Requirements: 2.1, 2.2_

- [ ] 4. ALB Target Group 헬스체크 설정 최적화
- [ ] 4.1 terraform/modules/alb/main.tf의 ECS 타겟 그룹 수정
  - 헬스체크 경로를 `/`에서 `/actuator/health`로 변경
  - Matcher를 `200-499`에서 `200`으로 변경
  - Healthy threshold를 적절히 조정 (2-3)
  - Unhealthy threshold를 3으로 설정
  - Interval을 15초로 설정
  - Timeout을 10초로 유지
  - Deregistration delay를 30초에서 60초로 증가
  - _Requirements: 1.1, 1.3, 1.4, 4.4_

- [ ] 5. ECS Service 설정 최적화
- [ ] 5.1 terraform/modules/ecs/main.tf의 ECS Service 수정
  - Health check grace period를 60초에서 180초로 증가
  - 배포 설정 확인 (maximum_percent: 200, minimum_healthy_percent: 100)
  - _Requirements: 1.2, 4.1, 4.2_

- [ ] 6. 보안 그룹 규칙 검증
- [ ] 6.1 보안 그룹 인바운드/아웃바운드 규칙 확인
  - ALB → ECS 태스크 (8080 포트) 허용 확인
  - ECS 태스크 → RDS (3306 포트) 허용 확인
  - ECS 태스크 아웃바운드 (외부 API 호출) 허용 확인
  - 필요시 보안 그룹 규칙 추가
  - _Requirements: 3.1, 3.2_

- [ ] 7. 애플리케이션 빌드 및 ECR 푸시
- [ ] 7.1 Gradle 빌드 실행
  - `./gradlew clean build` 실행
  - 빌드 성공 확인
  - _Requirements: 2.1_

- [ ] 7.2 Docker 이미지 빌드 및 ECR 푸시
  - Docker 이미지 빌드
  - ECR 로그인
  - 이미지 태그 지정 (latest)
  - ECR에 푸시
  - _Requirements: 2.4_

- [ ] 8. Terraform 적용 및 ECS 배포
- [ ] 8.1 Terraform plan 실행 및 변경 사항 확인
  - `terraform plan` 실행
  - ECS Task Definition, ALB Target Group, ECS Service 변경 확인
  - _Requirements: 1.1, 2.1, 4.1_

- [ ] 8.2 Terraform apply 실행
  - `terraform apply` 실행
  - 리소스 업데이트 완료 대기
  - _Requirements: 1.1, 2.1, 4.1_

- [ ] 9. 배포 검증 및 모니터링
- [ ] 9.1 ECS 태스크 상태 확인
  - AWS 콘솔 또는 CLI로 태스크 상태 확인
  - 태스크가 RUNNING 상태인지 확인
  - _Requirements: 4.2_

- [ ] 9.2 ALB 타겟 그룹 헬스체크 상태 확인
  - 타겟이 healthy 상태로 전환되는지 확인
  - 헬스체크 실패 시 CloudWatch Logs 확인
  - _Requirements: 1.1, 1.3, 4.3_

- [ ] 9.3 애플리케이션 엔드포인트 테스트
  - ALB DNS를 통해 `/actuator/health` 접근 테스트
  - `/dashboard` 등 주요 엔드포인트 테스트
  - 502/503/504 에러 발생 여부 확인
  - _Requirements: 1.5, 4.3_

- [ ]* 9.4 CloudWatch Logs 확인
  - ECS 태스크 로그에서 애플리케이션 시작 로그 확인
  - 데이터베이스 연결 성공 로그 확인
  - 에러 로그 여부 확인
  - _Requirements: 3.4, 5.1, 5.2, 5.4_

- [ ]* 9.5 메모리 및 CPU 사용률 모니터링
  - CloudWatch에서 ECS 서비스의 메모리 사용률 확인
  - CPU 사용률 확인
  - OOM 에러 발생 여부 확인
  - _Requirements: 2.1, 2.2, 2.3_

- [ ]* 10. 롤링 업데이트 테스트
  - 코드 변경 후 새 이미지 빌드 및 푸시
  - ECS 서비스 강제 업데이트
  - 배포 중 다운타임 없이 업데이트되는지 확인
  - 이전 태스크가 적절한 시점에 종료되는지 확인
  - _Requirements: 4.1, 4.2, 4.4_
