terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

provider "aws" {
  region = "ap-southeast-2"
}

# -------------------------------------------------------------------
# Variables
# -------------------------------------------------------------------

variable "key_pair_name" {
  description = "Name of the existing EC2 key pair for SSH access"
  type        = string
}

# -------------------------------------------------------------------
# Security Group — allow SSH (22) and game server (8000)
# -------------------------------------------------------------------

resource "aws_security_group" "game_server_sg" {
  name        = "comp3050-game-server-sg"
  description = "Allow SSH and game server traffic"

  ingress {
    description = "SSH"
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    description = "Game server"
    from_port   = 8000
    to_port     = 8000
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    description = "HTTP"
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name    = "comp3050-game-server-sg"
    Project = "COMP3050"
  }
}

# -------------------------------------------------------------------
# EC2 Instance — t3.micro, Amazon Linux 2023, Sydney
# -------------------------------------------------------------------

resource "aws_instance" "game_server" {
  ami                    = "ami-0892a9c01908fafd1"  # Amazon Linux 2023, ap-southeast-2
  instance_type          = "t3.micro"
  key_name               = var.key_pair_name
  vpc_security_group_ids = [aws_security_group.game_server_sg.id]

  user_data = <<-EOF
    #!/bin/bash
    dnf update -y
    dnf install -y docker
    systemctl start docker
    systemctl enable docker
    usermod -aG docker ec2-user
    docker run -d --restart always --name game-server \
      -p 8000:8000 \
      -e APP_USER=Baelin \
      hansmq/game-server
  EOF

  tags = {
    Name    = "comp3050-game-server"
    Project = "COMP3050"
    Team    = "TeamIBJK"
  }
}

# -------------------------------------------------------------------
# Elastic IP — static address pinned to EC2
# -------------------------------------------------------------------

resource "aws_eip" "game_server_eip" {
  instance = aws_instance.game_server.id
  domain   = "vpc"

  tags = {
    Name    = "comp3050-game-server-eip"
    Project = "COMP3050"
  }
}

# -------------------------------------------------------------------
# Outputs
# -------------------------------------------------------------------

output "server_ip" {
  description = "Public IP of the game server"
  value       = aws_eip.game_server_eip.public_ip
}

output "server_url" {
  description = "Game server URL"
  value       = "http://${aws_eip.game_server_eip.public_ip}:8000"
}

output "ssh_command" {
  description = "SSH command to connect to the server"
  value       = "ssh -i YOUR_KEY.pem ec2-user@${aws_eip.game_server_eip.public_ip}"
}
