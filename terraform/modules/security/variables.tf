# modules/security/variables.tf

variable "vpc_id" {
  description = "VPC ID to create security groups in"
  type        = string
}

variable "project_name" {
  description = "Project name for tagging"
  type        = string
}

variable "my_ip" {
  description = "Admin IP for SSH access (e.g., 1.2.3.4/32)"
  type        = string
}

variable "container_port" {
  description = "ECS container port"
  type        = number
}