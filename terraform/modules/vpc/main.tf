# modules/vpc/main.tf

variable "vpc_cidr" {
  description = "VPC에 사용할 CIDR 블록"
  default     = "10.0.0.0/16"
}

variable "project_name" {
  description = "태그에 사용할 프로젝트 이름"
}

# 1. VPC 생성
resource "aws_vpc" "main" {
  cidr_block           = var.vpc_cidr
  enable_dns_hostnames = true
  enable_dns_support   = true

  tags = {
    Name = "${var.project_name}-vpc"
  }
}

# 2. Public Subnets (모든 리소스가 여기 위치)
resource "aws_subnet" "public" {
  count                   = 2 # 2개 가용 영역에 생성
  vpc_id                  = aws_vpc.main.id
  cidr_block              = "10.0.1${count.index}.0/24"
  availability_zone       = element(["ap-northeast-2a", "ap-northeast-2c"], count.index)
  map_public_ip_on_launch = true

  tags = {
    Name = "${var.project_name}-public-subnet-${count.index + 1}"
  }
}

# 4. Internet Gateway (IGW)
resource "aws_internet_gateway" "main" {
  vpc_id = aws_vpc.main.id
  tags = {
    Name = "${var.project_name}-igw"
  }
}

# 7. Public Route Table
resource "aws_route_table" "public" {
  vpc_id = aws_vpc.main.id

  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.main.id
  }

  tags = {
    Name = "${var.project_name}-public-rt"
  }
}

# 8. Route Table 연결
resource "aws_route_table_association" "public" {
  count          = 2
  subnet_id      = aws_subnet.public[count.index].id
  route_table_id = aws_route_table.public.id
}

