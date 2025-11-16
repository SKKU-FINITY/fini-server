# infra-network/variables.tf

variable "my_ip" {
  description = "Admin IP for SSH access (e.g., 1.2.3.4/32)"
  type        = string
}