variable "vpc_id" {}
variable "subnets" { type = list(string) }
variable "security_groups" { type = list(string) }
variable "alb_certificate_arn" {}