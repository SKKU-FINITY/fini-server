variable "aws_region" {
  description = "AWS Region for CloudWatch logs"
  type        = string
}
variable "vpc_id" {}
variable "key_name" {}
variable "public_subnets" {
  description = "ECS instances/tasks will be placed in public subnets"
  type        = list(string)
}
variable "security_group_ids" { type = list(string) }
variable "ecr_image_url" { description = "ECR 리포지토리 URL (태그 제외)" }
variable "ecs_tg_arn" { description = "ECS용 타겟 그룹 ARN" }
variable "container_port" { default = 8080 }

variable "db_endpoint_url" {
  description = "RDS DB Endpoint (host:port)"
  type        = string
}
variable "secrets_manager_arn" {
  description = "AWS Secrets Manager ARN for app secrets"
  type        = string
}

variable "app_image_tag" {
  description = "The unique tag (timestamp) of the build."
  type        = string
  default     = "latest"
}