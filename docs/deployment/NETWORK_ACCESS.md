# Network Access Configuration for Musify

This guide explains how to access the Musify application from other computers on your network.

## Quick Deployment

1. **Run the deployment script:**
   ```bash
   ./deploy.sh
   ```
   
   This script will:
   - Deploy all services with external network access
   - Show you the IP addresses to use
   - Provide firewall configuration instructions

## Manual Access Configuration

### Services and Ports

After deployment, the following services will be accessible from other computers:

| Service | Port | External Access URL | Description |
|---------|------|-------------------|-------------|
| **Main Application (NGINX)** | 8080 | `http://YOUR_IP:8080` | Production-ready app |
| **Development Frontend** | 4200 | `http://YOUR_IP:4200` | Angular dev server |
| **Backend API** | 8443 | `https://YOUR_IP:8443` | Direct API access |
| **Database** | 5432 | `YOUR_IP:5432` | PostgreSQL database |
| **Testing Service** | 9090 | `http://YOUR_IP:9090` | Flaky service for demos |

### Finding Your IP Address

To find your machine's IP address:

```bash
# Linux/Mac
hostname -I | awk '{print $1}'

# Or use ip command
ip route get 1 | awk '{print $7}' | head -1

# Windows
ipconfig | findstr "IPv4"
```

### Firewall Configuration

**Ubuntu/Debian:**
```bash
sudo ufw allow 8080,4200,8443,5432,9090/tcp
```

**CentOS/RHEL/Fedora:**
```bash
sudo firewall-cmd --permanent --add-port=8080/tcp
sudo firewall-cmd --permanent --add-port=4200/tcp
sudo firewall-cmd --permanent --add-port=8443/tcp
sudo firewall-cmd --permanent --add-port=5432/tcp
sudo firewall-cmd --permanent --add-port=9090/tcp
sudo firewall-cmd --reload
```

**Windows:**
- Open Windows Defender Firewall
- Allow inbound rules for ports: 8080, 4200, 8443, 5432, 9090

## Testing External Access

1. **From another computer on the same network:**
   ```bash
   # Test the main application
   curl http://YOUR_IP:8080
   
   # Test the API health endpoint
   curl -k https://YOUR_IP:8443/actuator/health
   ```

2. **Open in browser from another computer:**
   - Main app: `http://YOUR_IP:8080`
   - Dev frontend: `http://YOUR_IP:4200`

## Troubleshooting

### Common Issues

1. **Connection refused:**
   - Check if containers are running: `docker compose ps`
   - Verify firewall settings
   - Ensure you're using the correct IP address

2. **Frontend can't connect to backend:**
   - The frontend is configured to automatically use the current hostname
   - Make sure you're accessing via IP, not localhost

3. **HTTPS certificate warnings:**
   - The backend uses a self-signed certificate
   - You may need to accept the certificate in your browser

### Checking Services

```bash
# View all running containers
docker compose ps

# Check logs for issues
docker compose logs -f

# Check specific service logs
docker compose logs nginx
docker compose logs backend-app-1
```

## Security Considerations

- The application uses HTTP (not HTTPS) for the main interface
- Database is exposed on port 5432 (consider restricting access in production)
- Self-signed certificates are used (not suitable for production)
- For production deployment, consider:
  - Using proper SSL certificates
  - Restricting database access
  - Using environment-specific configurations

## Network Architecture

```
Internet/Network
       │
   ┌───▼────┐
   │ NGINX  │ :8080 (HTTP)
   │        │
   └───┬────┘
       │
   ┌───▼────────────────┐
   │ Backend Replicas   │
   │ App-1 & App-2     │ :8443 (HTTPS)
   └───┬────────────────┘
       │
   ┌───▼────┐    ┌──────────┐
   │Postgres│    │ Flaky    │
   │   DB   │    │ Service  │
   └────────┘    └──────────┘
    :5432         :9090
```

The Angular frontend connects to NGINX (port 8080), which load-balances between the two backend replicas.