resource "aws_lb" "main" {
  name                     = "fini-server-alb"
  internal                 = false
  load_balancer_type       = "application"
  security_groups          = var.security_groups
  subnets                  = var.subnets
  enable_deletion_protection = false
  enable_http2             = true
  desync_mitigation_mode   = "defensive"
  idle_timeout             = 60
}

resource "aws_lb_target_group" "ecs_tg" {
  name                 = "fini-ecs-tg"
  port                 = 8080
  protocol             = "HTTP"
  vpc_id               = var.vpc_id
  target_type          = "instance"
  deregistration_delay = 30

  health_check {
    enabled  = true
    path     = "/"
    protocol = "HTTP"
    matcher  = "200-499"
    port     = "traffic-port"
  }
}

resource "aws_lb_listener" "http" {
  load_balancer_arn = aws_lb.main.arn
  port              = 80
  protocol          = "HTTP"

  default_action {
    type = "redirect"
    redirect {
      protocol   = "HTTPS"
      port       = "443"
      status_code = "HTTP_301"
    }
  }
}

resource "aws_lb_listener" "https" {
  load_balancer_arn = aws_lb.main.arn
  port              = 443
  protocol          = "HTTPS"
  certificate_arn   = var.alb_certificate_arn
  ssl_policy        = "ELBSecurityPolicy-TLS13-1-2-Res-2021-06"

  default_action {
    type = "forward"

    forward {
      target_group {
        arn = aws_lb_target_group.ecs_tg.arn
      }
      stickiness {
        enabled  = false
        duration = 3600
      }
    }
  }
}