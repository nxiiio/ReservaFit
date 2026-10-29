data "aws_ami" "ubuntu" {
  most_recent = true
  owners      = ["099720109477"] # Canonical

  filter {
    name   = "name"
    values = ["ubuntu/images/hvm-ssd-gp3/ubuntu-noble-24.04-amd64-server-*"]
  }
}

# Every \r is dropped: a file saved on Windows would otherwise end each line of the
# instance's .env and systemd unit with one
locals {
  user_data = replace(templatefile("${path.module}/cloud-init.yaml.tftpl", {
    compose_b64       = base64encode(replace(file("${path.module}/../compose.prod.yaml"), "\r", ""))
    caddyfile_b64     = base64encode(replace(file("${path.module}/../docker/Caddyfile"), "\r", ""))
    domain            = var.domain
    registry          = local.registry
    db_password       = var.db_password
    rabbitmq_password = var.rabbitmq_password
  }), "\r", "")
}

resource "aws_instance" "app" {
  ami                    = data.aws_ami.ubuntu.id
  instance_type          = var.instance_type
  subnet_id              = data.aws_subnet.default.id
  vpc_security_group_ids = [aws_security_group.app.id]
  # Pre-created by the lab (IAM roles cannot be created there). It lets the instance pull
  # from ECR and lets you open a shell with Session Manager, so there is no SSH key or port 22.
  iam_instance_profile = "LabInstanceProfile"

  # cloud-init installs Docker, writes the stack into /opt/reservafit and starts it.
  # It only runs on the first boot, so any change here replaces the instance, and with it
  # MySQL's data: gyms come back from data.sql, but users and bookings are lost.
  # Gzipped because AWS caps user data at 16 KB; cloud-init unpacks it.
  user_data_base64            = base64gzip(local.user_data)
  user_data_replace_on_change = true

  metadata_options {
    http_tokens = "required" # IMDSv2 only
  }

  root_block_device {
    volume_type = "gp3"
    volume_size = 30
    encrypted   = true
  }

  tags = {
    Name = "reservafit"
  }

  lifecycle {
    # A newer Ubuntu image must not replace the running instance (and its data)
    ignore_changes = [ami]
  }
}

# Keeps the public IP across lab sessions (the lab stops the instance when a session ends),
# so the DNS record of the domain only has to be set once
resource "aws_eip" "app" {
  domain   = "vpc"
  instance = aws_instance.app.id

  tags = {
    Name = "reservafit"
  }
}
