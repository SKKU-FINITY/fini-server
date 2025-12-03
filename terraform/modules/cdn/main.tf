terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
    }
  }
}

resource "aws_cloudfront_origin_request_policy" "vercel_policy" {
  name    = "vercel-no-host-policy"
  comment = "Policy for Vercel without custom domain setting"

  cookies_config {
    cookie_behavior = "all"
  }

  query_strings_config {
    query_string_behavior = "all"
  }

  headers_config {
    header_behavior = "whitelist"
    headers {
      items = [
        # Vercel이 정상 작동하기 위해 필요한 헤더들 (Host 제외)
        "Accept",
        "Accept-Language",
        "Origin",
        "Referer",
        "User-Agent",
        "Access-Control-Request-Method",
        "Access-Control-Request-Headers"
      ]
    }
  }
}

resource "aws_cloudfront_distribution" "s3_distribution" {
  provider = aws

  origin {
    domain_name = "skku-fini.vercel.app"
    origin_id   = "frontend"
    custom_origin_config {
      http_port              = 80
      https_port             = 443
      origin_protocol_policy = "https-only" # Vercel은 HTTPS 필수
      origin_ssl_protocols   = ["TLSv1.2"]
    }
  }

  origin {
    domain_name = var.alb_dns_name
    origin_id   = var.alb_dns_name
    custom_origin_config {
      http_port              = 80
      https_port             = 443
      origin_protocol_policy = "match-viewer"
      origin_ssl_protocols   = ["TLSv1.2"]
    }
  }

  enabled             = true
  is_ipv6_enabled     = true
  http_version        = "http2"
  price_class         = "PriceClass_All"
  aliases             = ["finiapp.tech"]
  wait_for_deployment = true

  # 기본 캐시 동작
  default_cache_behavior {
    target_origin_id         = "frontend"
    viewer_protocol_policy   = "redirect-to-https"

    allowed_methods = ["DELETE", "GET", "HEAD", "OPTIONS", "PATCH", "POST", "PUT"]
    cached_methods  = ["GET", "HEAD", "OPTIONS"]
    compress        = true

    cache_policy_id          = "4135ea2d-6df8-44a3-9df3-4b5a84be39ad" # Managed-CachingDisabled
    origin_request_policy_id = aws_cloudfront_origin_request_policy.vercel_policy.id
  }

  # 추가 캐시 동작 (/api/*)
  ordered_cache_behavior {
    path_pattern             = "/api/*"
    target_origin_id         = var.alb_dns_name# ALB Origin ID
    viewer_protocol_policy   = "redirect-to-https"
    allowed_methods          = ["DELETE", "GET", "HEAD", "OPTIONS", "PATCH", "POST", "PUT"]
    cached_methods           = ["GET", "HEAD"]
    compress                 = true
    cache_policy_id          = "83da9c7e-98b4-4e11-a168-04f0df8e2c65" # Managed-CachingOptimized
    origin_request_policy_id = "216adef6-5c7f-47e4-b989-5492eafa07d3" # Managed-AllViewer
  }

  # 추가 캐시 동작 (/swagger-ui/*)
  ordered_cache_behavior {
    path_pattern             = "/swagger-ui/*"
    target_origin_id         = var.alb_dns_name# ALB Origin ID
    viewer_protocol_policy   = "redirect-to-https"
    allowed_methods          = ["DELETE", "GET", "HEAD", "OPTIONS", "PATCH", "POST", "PUT"]
    cached_methods           = ["GET", "HEAD"]
    compress                 = true
    cache_policy_id          = "83da9c7e-98b4-4e11-a168-04f0df8e2c65" # Managed-CachingOptimized
    origin_request_policy_id = "216adef6-5c7f-47e4-b989-5492eafa07d3" # Managed-AllViewer
  }

  # 추가 캐시 동작 (/v3/*)
  ordered_cache_behavior {
    path_pattern             = "/v3/*"
    target_origin_id         = var.alb_dns_name# ALB Origin ID
    viewer_protocol_policy   = "redirect-to-https"
    allowed_methods          = ["DELETE", "GET", "HEAD", "OPTIONS", "PATCH", "POST", "PUT"]
    cached_methods           = ["GET", "HEAD"]
    compress                 = true
    cache_policy_id          = "83da9c7e-98b4-4e11-a168-04f0df8e2c65" # Managed-CachingOptimized
    origin_request_policy_id = "216adef6-5c7f-47e4-b989-5492eafa07d3" # Managed-AllViewer
  }

  viewer_certificate {
    acm_certificate_arn      = var.cdn_certificate_arn
    ssl_support_method       = "sni-only"
    minimum_protocol_version = "TLSv1.2_2021"
  }

  restrictions {
    geo_restriction {
      restriction_type = "none"
    }
  }

  tags = {
    Name = "fini-app"
  }
}