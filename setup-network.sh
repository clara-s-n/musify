#!/bin/bash

# Network setup script for Musify application
# This script helps configure your system for external access

echo "🌐 Musify Network Setup"
echo "======================="

# Get current IP address
HOST_IP=$(hostname -I | awk '{print $1}')
echo "📍 Your machine's IP address: $HOST_IP"
echo ""

# Check if running as root for firewall commands
if [[ $EUID -eq 0 ]]; then
   echo "⚠️  Running as root - firewall configuration available"
   FIREWALL_CMD=true
else
   echo "ℹ️  Not running as root - will show firewall commands to run manually"
   FIREWALL_CMD=false
fi

echo ""

# Detect the firewall system
if command -v ufw &> /dev/null; then
    FIREWALL_TYPE="ufw"
    echo "🔥 Detected firewall: UFW (Ubuntu/Debian)"
elif command -v firewall-cmd &> /dev/null; then
    FIREWALL_TYPE="firewall-cmd"
    echo "🔥 Detected firewall: firewalld (CentOS/RHEL/Fedora)"
else
    FIREWALL_TYPE="unknown"
    echo "🔥 No known firewall detected"
fi

echo ""

# Required ports
PORTS=(8080 4200 8443 5432 9090)
echo "🔌 Required ports for external access:"
for port in "${PORTS[@]}"; do
    echo "   • $port"
done

echo ""

# Configure firewall
if [ "$FIREWALL_CMD" = true ]; then
    echo "🛠️  Configuring firewall..."
    
    if [ "$FIREWALL_TYPE" = "ufw" ]; then
        echo "Enabling UFW and adding rules..."
        ufw --force enable
        for port in "${PORTS[@]}"; do
            ufw allow $port/tcp
            echo "   ✅ Allowed port $port"
        done
        ufw status
        
    elif [ "$FIREWALL_TYPE" = "firewall-cmd" ]; then
        echo "Adding firewalld rules..."
        for port in "${PORTS[@]}"; do
            firewall-cmd --permanent --add-port=$port/tcp
            echo "   ✅ Added port $port"
        done
        firewall-cmd --reload
        echo "   ✅ Firewall reloaded"
        firewall-cmd --list-ports
    fi
    
else
    echo "🛠️  Firewall configuration commands (run as root):"
    echo ""
    
    if [ "$FIREWALL_TYPE" = "ufw" ]; then
        echo "For UFW (Ubuntu/Debian):"
        echo "  sudo ufw enable"
        echo "  sudo ufw allow 8080,4200,8443,5432,9090/tcp"
        echo "  sudo ufw status"
        
    elif [ "$FIREWALL_TYPE" = "firewall-cmd" ]; then
        echo "For firewalld (CentOS/RHEL/Fedora):"
        for port in "${PORTS[@]}"; do
            echo "  sudo firewall-cmd --permanent --add-port=$port/tcp"
        done
        echo "  sudo firewall-cmd --reload"
        
    else
        echo "Manual firewall configuration required:"
        echo "  Allow incoming TCP connections on ports: ${PORTS[*]}"
    fi
fi

echo ""
echo "🔍 Testing network connectivity..."

# Test if ports are available
for port in "${PORTS[@]}"; do
    if netstat -tuln 2>/dev/null | grep -q ":$port "; then
        echo "   ✅ Port $port is in use (good if application is running)"
    else
        echo "   ⚠️  Port $port is available (start application to use it)"
    fi
done

echo ""
echo "📋 Next steps:"
echo "   1. Run './deploy.sh' to start the application"
echo "   2. Access from other computers using IP: $HOST_IP"
echo "   3. Main application: http://$HOST_IP:8080"
echo "   4. Development: http://$HOST_IP:4200"
echo ""

echo "🧪 Test external access:"
echo "   From another computer, run:"
echo "   curl http://$HOST_IP:8080"
echo ""

echo "✅ Network setup complete!"