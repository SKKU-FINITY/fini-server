# modules/dns/main.tf

variable "cloudfront_domain_name" {}

resource "aws_route53_zone" "main" {
  name = "finiapp.tech"
}

resource "aws_route53_record" "acm_validation_1" {
  zone_id = aws_route53_zone.main.zone_id
  name    = "_cff750a985d918e9ce0440d58b31c3cf.finiapp.tech"
  type    = "CNAME"
  ttl     = 300
  records = ["_2de032c93c6336c3bf164c0977096362.jkddzztszm.acm-validations.aws."]
}

resource "aws_route53_record" "acm_validation_2" {
  zone_id = aws_route53_zone.main.zone_id
  name    = "_770d89fab9951649dd0b1ab9ac9dcc49.api.finiapp.tech"
  type    = "CNAME"
  ttl     = 300
  records = ["_34b36b28bdd87325e148f6314a93250c.jkddzztszm.acm-validations.aws."]
}

resource "aws_route53_record" "api" {
  zone_id = aws_route53_zone.main.zone_id
  name    = "api.finiapp.tech"
  type    = "A"

  alias {
    name                   = var.cloudfront_domain_name
    zone_id                = "Z2FDTNDATAQYW2" # CloudFront 고정 Zone ID
    evaluate_target_health = false # 상태 파일 기준
  }
}

resource "aws_route53_record" "api_aaaa" {
  zone_id = aws_route53_zone.main.zone_id
  name    = "api.finiapp.tech"
  type    = "AAAA"

  alias {
    name                   = var.cloudfront_domain_name
    zone_id                = "Z2FDTNDATAQYW2" # CloudFront 고정 Zone ID
    evaluate_target_health = false # 상태 파일 기준
  }
}