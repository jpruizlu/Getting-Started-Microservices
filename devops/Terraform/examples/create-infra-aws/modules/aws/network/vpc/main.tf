# Create a VPC to launch build instances into
resource "aws_vpc" "vpc_id" {
  cidr_block           = "${var.cidr_block}"
  enable_dns_hostnames = true
  enable_dns_support   = true
  instance_tenancy     = "default"

  lifecycle {
    create_before_destroy = true
  }

  tags = "${
    map(
     "Name", "vpc-${var.cluster_name}",
     "kubernetes.io/cluster/${var.cluster_name}", "shared",
    )
  }"
}

# Create an internet gateway to give our subnet access to the outside world
resource "aws_internet_gateway" "gw_id" {
  vpc_id = "${aws_vpc.vpc_id.id}"

  tags {
    Name = "igw-${var.cluster_name}"
  }
}

# Create dhcp option setup
resource "aws_vpc_dhcp_options" "vpc_dhcp_id" {
  domain_name         = "us-west-2.compute.internal"
  domain_name_servers = ["AmazonProvidedDNS"]

  tags {
    Name = "dhcp-${var.cluster_name}"
  }
}