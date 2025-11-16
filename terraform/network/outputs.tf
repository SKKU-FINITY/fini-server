# infra-network/outputs.tf

# 1. Network 모듈의 Output을 그대로 전달
output "vpc_id" {
  value = module.vpc.vpc_id
}
output "public_subnet_ids" {
  value = module.vpc.public_subnet_ids
}
# 2. Security 모듈의 Output을 전달
output "alb_sg_id" {
  value = module.security.alb_sg_id
}
output "ecs_sg_id" {
  value = module.security.ecs_sg_id
}
output "rds_sg_id" {
  value = module.security.rds_sg_id
}