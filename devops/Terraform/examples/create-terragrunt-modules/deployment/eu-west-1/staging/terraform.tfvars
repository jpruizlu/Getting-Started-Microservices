terragrunt = {
  include {
    path = "${find_in_parent_folders()}"
  }

  terraform {
    source = "git::https://github.com/jsa4000/Getting-Started-Microservices.git//devops/Terraform/examples/create-terraform-modules/module/shared/openstack" # ?ref=0.1.0

    extra_arguments "conditional_vars" {
      commands            = ["${get_terraform_commands_that_need_vars()}"]
      optional_var_files  = [
        "${get_parent_tfvars_dir()}/../common.tfvars",
        "${get_parent_tfvars_dir()}/override.tfvars"
      ]
      arguments = [
        "-var-file=terraform.tfvars"
      ]
    }
  }
}

# Authentication Parameters (must use env variables instead)
user_name = "demo"
tenant_name = "demo"
password = "password"
domain_name = "Default"

# Module parameters
environment = "staging"
cidr        = "192.168.3.0/24"
auth_url = "http://10.0.0.10/identity/v3"