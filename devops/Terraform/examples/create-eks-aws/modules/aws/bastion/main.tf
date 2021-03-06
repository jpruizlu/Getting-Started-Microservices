
locals {
  tags = {
    Environment = "${var.environment}"
    Owner       = "${var.owner}"
    Workspace   = "${var.cluster_name}"
  }
}

data "aws_ami" "latest-ubuntu" {
  most_recent = true
  owners = ["099720109477"] # Canonical

    filter {
        name   = "name"
        values = ["ubuntu/images/hvm-ssd/ubuntu-xenial-16.04-amd64-server-*"]
    }

    filter {
        name   = "virtualization-type"
        values = ["hvm"]
    }
}

resource "aws_instance" "bastion" {
  ami                         = "${data.aws_ami.latest-ubuntu.id}"
  key_name                    = "${aws_key_pair.bastion_key.key_name}"
  instance_type               = "${var.instance_type}"
  subnet_id                   = "${var.subnet_id}"
  vpc_security_group_ids      = ["${aws_security_group.bastion_sec_group.id}"]
  associate_public_ip_address = true

  tags                        = "${merge(local.tags, map("Name", "${var.cluster_name}-bastion"))}"  
}

resource "aws_security_group" "bastion_sec_group" {
  name_prefix   = "bastion-sec-group"
  description   = "Security group to be applied to bastion"
  vpc_id        = "${var.vpc_id}"

  ingress {
    protocol    = "tcp"
    from_port   = 22
    to_port     = 22
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    protocol    = -1
    from_port   = 0 
    to_port     = 0 
    cidr_blocks = ["0.0.0.0/0"]
  }

   tags         = "${merge(local.tags, map("Name", "${var.cluster_name}-bastion_sec_group"))}"  
}

resource "aws_key_pair" "bastion_key" {
  key_name   = "${var.key_name}"
  public_key = "${var.public_key}"
}
