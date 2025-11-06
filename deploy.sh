#!/bin/bash

# Deploy script for Musify application
# This script deploys the application and makes it accessible from other computers

echo "🎵 Deploying Musify Application..."
echo "================================="

# Check if .env file exists
if [ ! -f ".env" ]; then
    echo "❌ Error: .env file not found!"
    echo "Please create a .env file with the required configuration."
    exit 1
fi

echo "✅ .env file found"

# Get the current machine's IP address
HOST_IP=$(hostname -I | awk '{print $1}')
echo "🌐 Host IP address: $HOST_IP"

# Stop any existing containers
echo "🛑 Stopping existing containers..."
docker compose down

# Build and start the application
echo "🚀 Building and starting the application..."
docker compose up --build -d

# Wait for services to be ready
echo "⏳ Waiting for services to be ready..."
sleep 10

# Check if services are running
echo "🔍 Checking service status..."
docker compose ps

echo ""
echo "🎉 Deployment complete!"
echo "================================="
echo ""
echo "🌐 Access the application from any computer:"
echo "   • Main application (NGINX): http://$HOST_IP:8080"
echo "   • Development frontend: http://$HOST_IP:4200"
echo "   • Backend API directly: https://$HOST_IP:8443"
echo "   • Database: $HOST_IP:5432"
echo ""
echo "📋 Local access (from this machine):"
echo "   • Main application: http://localhost:8080"
echo "   • Development frontend: http://localhost:4200"
echo ""
echo "🔧 Other services:"
echo "   • Flaky service (for testing): http://$HOST_IP:9090"
echo ""
echo "📖 To view logs: docker compose logs -f"
echo "⏹️  To stop: docker compose down"
echo ""
echo "🔥 Make sure your firewall allows connections on these ports!"
echo "   • For Ubuntu/Debian: sudo ufw allow 8080,4200,8443,5432,9090/tcp"
echo ""