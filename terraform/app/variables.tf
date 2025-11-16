variable "secrets_manager_arn" {
  description = "ECS가 사용할 Secrets Manager의 ARN"
  type        = string
}

variable "alb_certificate_arn" {
  description = "ALB (ap-northeast-2)에 사용할 ACM 인증서 ARN"
  type        = string
}

variable "cdn_certificate_arn" {
  description = "CloudFront (us-east-1)에 사용할 ACM 인증서 ARN"
  type        = string
}

variable "route53_zone_name" {
  description = "Route 53에 등록할 도메인 이름"
  type        = string
  default     = "finiapp.tech"
}

variable "app_image_tag" {
  description = "The specific image tag (timestamp) to deploy, passed from CI/CD."
  type        = string
  default     = "latest" # CI/CD가 아닌 로컬 apply 시 :latest를 사용
}

variable "my_ip" {}
variable "db_snapshot_identifier" {}
variable "db_password" {}