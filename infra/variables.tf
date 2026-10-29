variable "domain" {
  description = "Public host name of the app, without https:// (e.g. reservafit.duckdns.org). Its A record must point to the public_ip output."
  type        = string

  validation {
    condition     = can(regex("^[a-z0-9]([a-z0-9-]*[a-z0-9])?(\\.[a-z0-9]([a-z0-9-]*[a-z0-9])?)+$", var.domain))
    error_message = "domain must be a bare host name such as reservafit.duckdns.org (no scheme, no path)."
  }
}

variable "db_password" {
  description = "Password of the reservafit MySQL user. Letters and digits only, 16+ characters (openssl rand -hex 24)."
  type        = string
  sensitive   = true

  validation {
    condition     = can(regex("^[A-Za-z0-9]{16,}$", var.db_password))
    error_message = "db_password must be 16 or more letters and digits (it is written into a .env file unquoted)."
  }
}

variable "rabbitmq_password" {
  description = "Password of the reservafit RabbitMQ user. Letters and digits only, 16+ characters (openssl rand -hex 24)."
  type        = string
  sensitive   = true

  validation {
    condition     = can(regex("^[A-Za-z0-9]{16,}$", var.rabbitmq_password))
    error_message = "rabbitmq_password must be 16 or more letters and digits (it is written into a .env file unquoted)."
  }
}

variable "instance_type" {
  description = "EC2 instance type. The lab allows nano to large; the stack needs about 3 GB, so medium is the smallest that fits."
  type        = string
  default     = "t3.medium"
}

variable "availability_zone" {
  description = "Availability zone of the instance. Fixed because not every us-east-1 zone offers t3 instances."
  type        = string
  default     = "us-east-1a"
}
