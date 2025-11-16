# modules/rds/variables.tf

variable "db_identifier" {
  description = "RDS 인스턴스의 고유 식별자"
  type        = string
}

variable "db_password" {
  description = "RDS 마스터 사용자의 비밀번호"
  type        = string
  sensitive   = true
}

variable "db_subnet_group_name" {
  description = "RDS DB 서브넷 그룹 이름"
  type        = string
}

variable "db_subnet_ids" {
  description = "DB 서브넷 그룹에 포함될 서브넷 ID 목록"
  type        = list(string)
}

variable "vpc_security_group_ids" {
  description = "RDS에 적용할 보안 그룹 ID 목록"
  type        = list(string)
}

variable "db_snapshot_identifier" {
  description = "복원할 RDS 스냅샷의 이름"
  type        = string
  default     = null # 필수가 아님을 표시
}