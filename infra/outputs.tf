output "public_ip" {
  description = "Point the domain's A record (DuckDNS) here"
  value       = aws_eip.app.public_ip
}

output "url" {
  description = "Public URL of the app; also the SPA redirect URI to register in Azure"
  value       = "https://${var.domain}"
}

output "registry" {
  description = "ECR registry the images are pushed to"
  value       = local.registry
}

output "instance_id" {
  description = "EC2 instance (Session Manager: EC2 console > Connect > Session Manager)"
  value       = aws_instance.app.id
}
