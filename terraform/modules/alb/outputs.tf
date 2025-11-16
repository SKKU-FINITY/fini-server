output "alb_dns_name" {
  value = aws_lb.main.dns_name
}

output "ecs_tg_arn" {
  value = aws_lb_target_group.ecs_tg.arn
}