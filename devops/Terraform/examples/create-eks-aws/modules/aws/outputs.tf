
output "eks_cluster_endpoint" {
  description = "Endpoint for EKS control plane."
  value       = "${module.eks.cluster_endpoint}"
}

output "eks_cluster_security_group_id" {
  description = "Security group ids attached to the cluster control plane."
  value       = "${module.eks.cluster_security_group_id}"
}

output "eks_kubectl_config" {
  description = "kubectl config as generated by the module."
  value       = "${module.eks.kubectl_config}"
}

output "eks_config_map_aws_auth" {
  description = ""
  value       = "${module.eks.config_map_aws_auth}"
}

output "bastion_public_ip" {
  value = "${module.bastion.public_ip}"
}