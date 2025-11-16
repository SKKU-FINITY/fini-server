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

data "terraform_remote_state" "app" {
  backend = "local"
  config = {
    path = "../app/terraform.tfstate"
  }
}

# --- DNS 모듈 호출 ---
module "dns" {
  source = "../modules/dns"

  # infra-app에서 읽어온 CloudFront 도메인 이름을 전달
  cloudfront_domain_name = data.terraform_remote_state.app.outputs.cloudfront_domain_name
}