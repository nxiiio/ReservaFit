terraform {
  required_version = ">= 1.10"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 6.0"
    }
  }

  # State lives in an S3 bucket of the lab account, never in git (it holds the passwords).
  # Bucket, key and region come from backend.hcl (copy backend.hcl.example):
  #   terraform init -backend-config=backend.hcl
  backend "s3" {}
}

provider "aws" {
  # The Learner Lab only allows us-east-1 and us-west-2, and the vockey key pair and the
  # rest of the lab setup live in us-east-1
  region = local.region

  # Lets the lab's Tag Editor list everything this stack created
  default_tags {
    tags = {
      Project   = "reservafit"
      ManagedBy = "terraform"
    }
  }
}

locals {
  region = "us-east-1"
}
