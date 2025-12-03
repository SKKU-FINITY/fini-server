terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }

  backend "s3" {
    bucket = "fini-app-bucket" # 1번에서 만든 버킷 이름
    key = "app/terraform.tfstate" 
    region = "ap-northeast-2"
  }
}

# 1. 기본 프로바이더 (서울 리전)
provider "aws" {
  region = "ap-northeast-2"
}

# 2. 별칭(Alias) 프로바이더 (버지니아 북부 리전)
provider "aws" {
  alias  = "us-east-1"
  region = "us-east-1"
}

# 3. network 스택의 상태 파일 읽기
data "terraform_remote_state" "network" {
  backend = "s3"
  config = {
    bucket = "fini-app-bucket" # 동일한 버킷 이름
    key    = "network/terraform.tfstate"   # network 모듈의 S3 키
    region = "ap-northeast-2"
  }
}

data "terraform_remote_state" "data" {
  backend = "s3"
  config = {
    bucket = "fini-app-bucket"
    key    = "data/terraform.tfstate"      # data 모듈의 S3 키
    region = "ap-northeast-2"
  }
}

locals {
  vpc_id          = data.terraform_remote_state.network.outputs.vpc_id
  public_subnets  = data.terraform_remote_state.network.outputs.public_subnet_ids
}

# --- ECR ---
module "ecr" {
  source = "../modules/ecr"
}

# --- ALB ---
module "alb" {
  source = "../modules/alb"
  vpc_id  = local.vpc_id

  subnets = local.public_subnets
  security_groups = [data.terraform_remote_state.network.outputs.alb_sg_id]
  alb_certificate_arn = var.alb_certificate_arn
}

# --- CDN (CloudFront) ---
module "cdn" {
  source = "../modules/cdn"

  providers = {
    aws = aws.us-east-1
  }
  alb_dns_name        = module.alb.alb_dns_name
  api_domain_name     = var.route53_zone_name
  cdn_certificate_arn = var.cdn_certificate_arn
}

# --- ECS ---
module "ecs" {
  source = "../modules/ecs"
  vpc_id = local.vpc_id
  public_subnets = local.public_subnets

  security_group_ids = [data.terraform_remote_state.network.outputs.ecs_sg_id]
  ecr_image_url = module.ecr.repository_url
  ecs_tg_arn    = module.alb.ecs_tg_arn

  aws_region = "ap-northeast-2"

  db_endpoint_url = data.terraform_remote_state.data.outputs.db_endpoint
  secrets_manager_arn = var.secrets_manager_arn
  key_name = "fini-key-pair"

  app_image_tag = var.app_image_tag
}