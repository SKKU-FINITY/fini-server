
variable "db_password" {
  description = "RDS 마스터 사용자의 비밀번호"
  type        = string
  sensitive   = true # 이 변수는 plan/apply 시 터미널에 노출되지 않습니다.
}

variable "db_snapshot_identifier" {
  description = "복원할 RDS 스냅샷의 이름"
  type        = string
}