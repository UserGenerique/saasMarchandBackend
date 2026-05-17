#!/bin/bash
set -e

echo "=== TissuGest VPS Setup ==="

# 1. Update system
echo ">> Updating system..."
apt update && apt upgrade -y

# 2. Install Docker
echo ">> Installing Docker..."
curl -fsSL https://get.docker.com | sh
systemctl enable docker
systemctl start docker

# 3. Install Docker Compose plugin
echo ">> Installing Docker Compose..."
apt install -y docker-compose-plugin

# 4. Create app directory
echo ">> Setting up app directory..."
mkdir -p /opt/tissugest
cd /opt/tissugest

# 5. Create .env file (edit this!)
if [ ! -f .env ]; then
    cat > .env << 'EOF'
DB_NAME=tissugest
DB_USER=tissugest
DB_PASSWORD=$(openssl rand -base64 32)
JWT_SECRET=$(openssl rand -base64 64)
CORS_ORIGINS=http://$(curl -s ifconfig.me)
REDIS_HOST=redis
REDIS_PORT=6379
EOF
    echo ">> .env file created. EDIT IT before deploying!"
    echo ">> Run: nano /opt/tissugest/.env"
fi

# 6. Firewall
echo ">> Configuring firewall..."
ufw allow 22/tcp   # SSH
ufw allow 80/tcp   # HTTP
ufw allow 443/tcp  # HTTPS
ufw --force enable

echo ""
echo "=== Setup complete! ==="
echo ""
echo "Next steps:"
echo "  1. Clone your repo: git clone <your-repo> /opt/tissugest/app"
echo "  2. Edit .env: nano /opt/tissugest/.env"
echo "  3. Copy docker-compose: cp /opt/tissugest/app/docker-compose.prod.yml /opt/tissugest/docker-compose.yml"
echo "  4. Deploy: cd /opt/tissugest && docker compose up -d --build"
echo ""
