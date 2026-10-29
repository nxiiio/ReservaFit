# One repository per image; scripts/build-push.sh builds and pushes the same list.
# Your lab credentials can push; the instance's LabRole can only pull, which is all it needs.
locals {
  images = ["frontend", "api-gateway", "ms-usuarios", "ms-gimnasios", "ms-reservas", "ms-notificaciones"]
}

data "aws_caller_identity" "current" {}

locals {
  registry = "${data.aws_caller_identity.current.account_id}.dkr.ecr.${local.region}.amazonaws.com"
}

resource "aws_ecr_repository" "app" {
  for_each = toset(local.images)

  name = "reservafit/${each.key}"
  # Every push moves the latest tag, which is what the instance pulls
  image_tag_mutability = "MUTABLE"
  # terraform destroy also deletes the pushed images
  force_delete = true
}

# Storage is billed per GB: keep only the last few builds of each image
resource "aws_ecr_lifecycle_policy" "app" {
  for_each = aws_ecr_repository.app

  repository = each.value.name
  policy = jsonencode({
    rules = [{
      rulePriority = 1
      description  = "Keep the last 5 images"
      selection = {
        tagStatus   = "any"
        countType   = "imageCountMoreThan"
        countNumber = 5
      }
      action = { type = "expire" }
    }]
  })
}
