# Terraform — Infrastructure as Code

Provisions all AWS infrastructure for the COMP3050 game server.

## Resources Created

| Resource | Config |
|----------|--------|
| EC2 instance | t3.micro, Amazon Linux 2023, ap-southeast-2 (Sydney) |
| Security group | Port 22 (SSH), 80 (HTTP), 8000 (game server) open to 0.0.0.0/0 |
| Elastic IP | Static public IP pinned to EC2 |

## Usage

```bash
# Initialise (first time only)
terraform init

# Preview changes
terraform plan -var="key_pair_name=YOUR_KEY"

# Provision all resources (~60 seconds)
terraform apply -var="key_pair_name=YOUR_KEY"

# Tear down all resources (zero ongoing AWS cost)
terraform destroy -var="key_pair_name=YOUR_KEY"
```

## Notes

- `terraform.tfstate` is excluded from git (contains sensitive resource IDs)
- `APP_PASS` is managed via GitHub Secrets and injected at deploy time by CI/CD
- Always run `terraform destroy` after testing to avoid unnecessary AWS charges
