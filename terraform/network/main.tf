# infra-network/main.tf

terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }

  backend "s3" {
    bucket = "fini-app-bucket" # 1번에서 만든 버킷 이름
    key = "network/terraform.tfstate"      # ★★★ 각 모듈별로 경로를 다르게 설정! (예: "network/terraform.tfstate")
    region = "ap-northeast-2"
  }
}

provider "aws" {
  region = "ap-northeast-2"
}

module "vpc" {
  source       = "../modules/vpc"
  project_name = "fini"
}

# 'network' 모듈의 VPC ID를 'security' 모듈에 전달
module "security" {
  source       = "../modules/security"
  project_name = "fini"

  vpc_id = module.vpc.vpc_id # 'network' 모듈의 vpc_id 출력값을 사용
  my_ip  = var.my_ip             # .tfvars 파일에서 값을 받음

  # Spring 컨테이너 포트
  container_port = 8080
}