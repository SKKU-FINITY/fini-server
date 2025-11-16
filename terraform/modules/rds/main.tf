resource "aws_db_subnet_group" "default" {
  name       = var.db_subnet_group_name
  subnet_ids = var.db_subnet_ids

  tags = {
    Name = "${var.db_identifier}-subnet-group"
  }
}

# RDS 데이터베이스 인스턴스 리소스입니다.
resource "aws_db_instance" "main" {
  # --- 기본 사양 및 식별자 ---
  identifier           = var.db_identifier
  snapshot_identifier  = var.db_snapshot_identifier
  instance_class       = "db.t4g.micro"
  engine               = "mysql"

  # --- 스토리지 ---
  allocated_storage    = 20
  storage_type         = "gp2"
  storage_encrypted    = true
  kms_key_id           = "arn:aws:kms:ap-northeast-2:077540774425:key/24787019-cb75-4d81-8f26-27ba083b3791"

  # --- 자격 증명 (비밀번호는 import되지 않습니다) ---
  username = "admin"
  password = var.db_password # 비밀번호는 변수로 안전하게 전달받습니다.

  # --- 네트워킹 및 보안 ---
  db_subnet_group_name   = aws_db_subnet_group.default.name
  vpc_security_group_ids = var.vpc_security_group_ids
  publicly_accessible    = true # ⚠️ 주의: 프로덕션 환경에서는 false를 권장합니다.

  lifecycle {
    ignore_changes = [
      # 'password' 속성은 Terraform이 관리/변경하지 않도록 무시합니다.
      password
    ]
  }

  # --- 백업 및 유지보수 ---
  backup_retention_period   = 0 # 백업 비활성화
  skip_final_snapshot       = true
  auto_minor_version_upgrade= false
  maintenance_window        = "fri:17:36-fri:18:06"
  backup_window             = "16:29-16:59"

  # --- 기타 설정 ---
  parameter_group_name      = "default.mysql8.0"
  option_group_name         = "default:mysql-8-0"
  copy_tags_to_snapshot     = true
  deletion_protection       = false
  multi_az                  = false
}
