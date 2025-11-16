# --- DB Outputs ---
output "db_endpoint" {
  description = "RDS 인스턴스 엔드포인트"
  value       = module.rds.endpoint
}
output "db_port" {
  description = "RDS 인스턴스 포트"
  value       = module.rds.port
}
