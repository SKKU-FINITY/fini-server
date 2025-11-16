terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}
provider "aws" {
  region = "ap-northeast-2"
}

data "terraform_remote_state" "network" {
  backend = "local"
  config = {
    path = "../network/terraform.tfstate"
  }
}

# locals 블록: Network 스택의 값 사용
locals {
  vpc_id          = data.terraform_remote_state.network.outputs.vpc_id
  public_subnets = data.terraform_remote_state.network.outputs.public_subnet_ids
  rds_sg_id       = data.terraform_remote_state.network.outputs.rds_sg_id
}

module "rds" {
  source = "../modules/rds"

  db_identifier          = "fini-db"
  db_password            = var.db_password # variables.tf에서 값 전달

  db_subnet_group_name   = "fini-db-subnet-group" # RDS 모듈에서 이 이름으로 생성
  db_subnet_ids          = local.public_subnets

  vpc_security_group_ids = [local.rds_sg_id]
  db_snapshot_identifier = var.db_snapshot_identifier
}