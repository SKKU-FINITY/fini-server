output "repository_url" {
  description = "ECR 리포지토리의 URL"
  value       = aws_ecr_repository.app.repository_url
}