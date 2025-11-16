# modules/network/variables.tf

# ... (기존 vpc_cidr, project_name 변수) ...

variable "my_ip" {
  description = "SSH 접속을 허용할 관리자 IP 주소 (CIDR 형식, 예: 1.2.3.4/32)"
  type        = string
  # ⭐️ 보안을 위해 실제 IP를 입력하세요. 0.0.0.0/0은 위험합니다.
  default     = "0.0.0.0/0"
}

variable "container_port" {
  description = "ECS Spring Boot 컨테이너가 사용하는 포트"
  type        = number
  default     = 8080
}