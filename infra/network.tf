# The lab account's default VPC and its default subnet in the chosen zone (public: it routes
# to an internet gateway and assigns public IPs)
data "aws_vpc" "default" {
  default = true
}

data "aws_subnet" "default" {
  vpc_id            = data.aws_vpc.default.id
  availability_zone = var.availability_zone
  default_for_az    = true
}

resource "aws_security_group" "app" {
  name        = "reservafit"
  description = "ReservaFit: only Caddy (HTTP and HTTPS) is reachable from the internet"
  vpc_id      = data.aws_vpc.default.id

  # Port 80 must stay open to everyone: Let's Encrypt validates the domain over it, and Caddy
  # redirects every other request to HTTPS. No SSH: use Session Manager (LabInstanceProfile).
  ingress {
    description = "HTTP (ACME challenge and redirect to HTTPS)"
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    description = "HTTPS"
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    description = "HTTP/3"
    from_port   = 443
    to_port     = 443
    protocol    = "udp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    description = "All outbound: packages, ECR, Lets Encrypt, Microsoft JWKS, Session Manager"
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}
