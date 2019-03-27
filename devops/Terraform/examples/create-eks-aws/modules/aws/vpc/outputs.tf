
# VPC
output "vpc_id" {
  description = "The ID of the VPC"
  value       = "${module.vpc.vpc_id}"
}

# Subnets
output "private_subnets" {
  description = "List of IDs of private subnets"
  value       = ["${module.vpc.private_subnets}"]
}

output "public_subnets" {
  description = "List of IDs of public subnets"
  value       = ["${module.vpc.public_subnets}"]
}

output "database_subnets" {
  description = "List of IDs of database subnets"
  value       = ["${module.vpc.database_subnets}"]
}

# NAT gateways
output "nat_public_ips" {
  description = "List of public Elastic IPs created for AWS NAT Gateway"
  value       = ["${module.vpc.nat_public_ips}"]
}

output "database_sec_group" {
  description = "database security group"
  value       = "${aws_security_group.database_sec_group.name}"
}

output "level_one_sec_group" {
  description = "Security to be applied to all unix machines"
  value       = "${aws_security_group.level_one_sec_group.id}"
}

output "level_two_sec_group" {
  description = "Security to be applied to some unix machines"
  value       = "${aws_security_group.level_two_sec_group.id}"
}

output "mgmt_sec_group" {
  description = "Security to be applied for management purposes"
  value       = "${aws_security_group.mgmt_sec_group.id}"
}
