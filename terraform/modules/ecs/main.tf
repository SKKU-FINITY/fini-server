locals {
  # [수정] CI/CD에서 태그를 받으면 그 태그를 사용하고, 아니면 'latest'를 사용합니다.
  effective_image_tag = (var.app_image_tag != null && var.app_image_tag != "latest" && var.app_image_tag != "") ? var.app_image_tag : "latest"

  effective_image_uri = "${var.ecr_image_url}:${local.effective_image_tag}"
}

# 1. CloudWatch 로그 그룹 (참조되기 전에 정의)
resource "aws_cloudwatch_log_group" "ecs_logs" {
  name              = "/ecs/fini-app"
  retention_in_days = 7
}

# 2. IAM 역할: Task Execution Role (ECR, Secrets, Logs 접근)
resource "aws_iam_role" "ecs_task_execution_role" {
  name = "fini-ecs-task-execution-role"
  assume_role_policy = jsonencode({
    Version   = "2012-10-17",
    Statement = [{
      Action    = "sts:AssumeRole",
      Effect    = "Allow",
      Principal = { Service = "ecs-tasks.amazonaws.com" }
    }]
  })
}

resource "aws_iam_role_policy" "ecs_task_execution_policy" {
  name = "ecs-task-execution-policy"
  role = aws_iam_role.ecs_task_execution_role.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      # 1. ECR 기본 권한
      {
        Effect = "Allow",
        Action = [
          "ecr:GetAuthorizationToken",
          "ecr:BatchCheckLayerAvailability",
          "ecr:GetDownloadUrlForLayer",
          "ecr:BatchGetImage"
        ],
        Resource = "*" # ECR은 모든 리소스(*)에 대해 허용 필요
      },
      # 2. Secrets Manager 접근 권한
      {
        Effect = "Allow",
        Action = [
          "secretsmanager:GetSecretValue"
        ],
        Resource = var.secrets_manager_arn
      },
      # 3. CloudWatch Logs 접근 권한
      {
        Effect = "Allow",
        Action = [
          "logs:CreateLogStream",
          "logs:PutLogEvents"
        ],
        Resource = "${aws_cloudwatch_log_group.ecs_logs.arn}:*"
      }
    ]
  })
}

# 3. IAM 역할: EC2 Instance Role (ECS 클러스터 등록)
resource "aws_iam_role" "ecs_instance_role" {
  name = "fini-ecs-instance-role"
  assume_role_policy = jsonencode({
    Version   = "2012-10-17",
    Statement = [{
      Action    = "sts:AssumeRole",
      Effect    = "Allow",
      Principal = { Service = "ec2.amazonaws.com" }
    }]
  })
}

resource "aws_iam_role_policy_attachment" "ecs_instance_role_policy" {
  role       = aws_iam_role.ecs_instance_role.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonEC2ContainerServiceforEC2Role"
}

resource "aws_iam_instance_profile" "ecs_instance_profile" {
  name = "fini-ecs-instance-profile"
  role = aws_iam_role.ecs_instance_role.name
}

# 4. ECS 최적화 AMI 검색
data "aws_ssm_parameter" "ecs_ami" {
  name = "/aws/service/ecs/optimized-ami/amazon-linux-2/recommended/image_id"
}

# 5. ECS 클러스터 생성
resource "aws_ecs_cluster" "main" {
  name = "fini-cluster"
}

# 6. EC2 Launch Template (Private Subnet용)
resource "aws_launch_template" "ecs_lt" {
  name          = "fini-ecs-lt"
  image_id      = data.aws_ssm_parameter.ecs_ami.value
  instance_type = "t3.small"
  iam_instance_profile {
    name = aws_iam_instance_profile.ecs_instance_profile.name
  }
  user_data = base64encode(<<-EOF
    #!/bin/bash
    echo ECS_CLUSTER=${aws_ecs_cluster.main.name} >> /etc/ecs/ecs.config
  EOF
  )

  network_interfaces {
    security_groups = var.security_group_ids
    associate_public_ip_address = true
  }

  key_name = var.key_name
}

# 7. Auto Scaling Group (Private Subnet용)
resource "aws_autoscaling_group" "ecs_asg" {
  name = "fini-ecs-asg"
  vpc_zone_identifier = var.public_subnets

  desired_capacity = 1
  min_size         = 1
  max_size         = 2 # 배포 시 2개까지 허용

  launch_template {
    id      = aws_launch_template.ecs_lt.id
    version = "$Latest"
  }

  # EC2 인스턴스를 시작하기 전에 클러스터가 먼저 존재하도록 보장합니다.
  depends_on = [aws_ecs_cluster.main]
}

# 8. 태스크 정의 (Bridge 모드)
resource "aws_ecs_task_definition" "app" {
  family                   = "fini-app-task"
  network_mode             = "bridge"
  requires_compatibilities = ["EC2"]
  cpu                      = "512"
  memory                   = "512"
  execution_role_arn       = aws_iam_role.ecs_task_execution_role.arn

  container_definitions = jsonencode([
    {
      name      = "fini-app-container"
      image     = local.effective_image_uri
      cpu       = 512
      memory    = 512
      essential = true

      logConfiguration = {
        logDriver = "awslogs"
        options = {
          "awslogs-group"         = aws_cloudwatch_log_group.ecs_logs.name
          "awslogs-region"        = var.aws_region
          "awslogs-stream-prefix" = "ecs"
        }
      }

      portMappings = [
        {
          containerPort = var.container_port
          hostPort      = 0
          protocol      = "tcp"
        }
      ]

      secrets = [
        { name = "DB_URL", valueFrom = "${var.secrets_manager_arn}:DB_URL::" },
        { name = "DB_USERNAME", valueFrom = "${var.secrets_manager_arn}:DB_USERNAME::" },
        { name = "DB_PASSWORD", valueFrom = "${var.secrets_manager_arn}:DB_PASSWORD::" },
        { name = "JWT_SECRET", valueFrom = "${var.secrets_manager_arn}:JWT_SECRET::" },
        { name = "FSS_API_KEY", valueFrom = "${var.secrets_manager_arn}:FSS_API_KEY::" },
        { name = "NAVER_API_ID", valueFrom = "${var.secrets_manager_arn}:NAVER_API_ID::" },
        { name = "NAVER_API_SECRET", valueFrom = "${var.secrets_manager_arn}:NAVER_API_SECRET::" },
        { name = "GEMINI_API_KEY", valueFrom = "${var.secrets_manager_arn}:GEMINI_API_KEY::" }
      ]
    }
  ])
}

# 9. ECS 서비스 (Bridge 모드)
resource "aws_ecs_service" "main" {
  name            = "fini-app-service"
  cluster         = aws_ecs_cluster.main.id
  task_definition = aws_ecs_task_definition.app.arn
  launch_type     = "EC2"
  desired_count   = 1

  deployment_maximum_percent         = 200
  deployment_minimum_healthy_percent = 100
  health_check_grace_period_seconds  = 60
  force_new_deployment = true

  load_balancer {
    target_group_arn = var.ecs_tg_arn
    container_name   = "fini-app-container"
    container_port   = var.container_port
  }

  depends_on = [aws_autoscaling_group.ecs_asg]
}