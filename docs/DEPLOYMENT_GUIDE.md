# 🚀 Guía de Despliegue y Acceso Externo

> **Configuración completa para desplegar Musify y acceder desde la red**

## ⚡ Despliegue Rápido

```bash
# Opción 1: Script automatizado
./deploy.sh

# Opción 2: Manual
docker compose up --build
```

## 🌐 Acceso desde la Red

### Servicios y Puertos

Después del despliegue, los siguientes servicios estarán accesibles desde otros equipos:

| Servicio | Puerto | URL Externa | Descripción |
|----------|--------|-------------|-------------|
| **Aplicación Principal (NGINX)** | 8080 | `http://TU_IP:8080` | App lista para producción |
| **Frontend Desarrollo** | 4200 | `http://TU_IP:4200` | Servidor Angular dev |
| **Backend API** | 8443 | `https://TU_IP:8443` | Acceso directo a API |
| **Base de Datos** | 5432 | `TU_IP:5432` | PostgreSQL |
| **Servicio de Pruebas** | 9090 | `http://TU_IP:9090` | Flaky service para demos |

### Obtener tu IP

```bash
# Linux/macOS
ip addr show | grep 'inet ' | grep -v '127.0.0.1'

# Windows
ipconfig | findstr "IPv4"

# Automático con el script
./deploy.sh  # Te muestra las IPs automáticamente
```

## 🔧 Configuración de Docker Compose

### Port Bindings para Acceso Externo

```yaml
# docker-compose.yaml
services:
  backend-app-1:
    ports:
      - "0.0.0.0:8443:8443"  # Acceso externo habilitado
  
  flaky-service:
    ports:
      - "0.0.0.0:9090:9090"  # Acceso externo habilitado
  
  postgres:
    ports:
      - "0.0.0.0:5432:5432"  # Acceso externo habilitado
  
  nginx:
    ports:
      - "0.0.0.0:8080:80"    # Ya configurado
  
  angular-frontend:
    ports:
      - "0.0.0.0:4200:4200"  # Ya configurado
```

### Variables de Entorno

```bash
# .env (opcional, para personalizar)
EXTERNAL_HOST=192.168.1.100
POSTGRES_HOST=postgres
JWT_SECRET=your-secret-key
SPOTIFY_CLIENT_ID=your-spotify-client-id
SPOTIFY_CLIENT_SECRET=your-spotify-client-secret
```

## 🌍 Configuración del Frontend

### Configuración Dinámica de Host

Los archivos de environment están configurados para detectar automáticamente la IP:

```typescript
// environment.ts, environment.development.ts
export const environment = {
  production: false,
  apiBaseUrl: `http://${window.location.hostname}:8080`
};
```

**Archivos actualizados:**
- `frontend/MusifyFront/src/environments/environment.ts`
- `frontend/MusifyFront/src/environments/environment.development.ts`
- `frontend/MusifyFront/src/app/enviroment/enviroment.ts`

### Configuración Manual (si necesitas IP específica)

```typescript
// Para IP fija
export const environment = {
  production: true,
  apiBaseUrl: 'http://192.168.1.100:8080'
};
```

## 🛡️ Configuración de Firewall

### Ubuntu/Debian

```bash
# Abrir puertos necesarios
sudo ufw allow 8080/tcp   # NGINX (aplicación principal)
sudo ufw allow 4200/tcp   # Frontend desarrollo
sudo ufw allow 8443/tcp   # Backend API
sudo ufw allow 5432/tcp   # PostgreSQL (opcional)
sudo ufw allow 9090/tcp   # Flaky service (para demos)

# Verificar reglas
sudo ufw status
```

### CentOS/RHEL/Fedora

```bash
# Firewalld
sudo firewall-cmd --permanent --add-port=8080/tcp
sudo firewall-cmd --permanent --add-port=4200/tcp
sudo firewall-cmd --permanent --add-port=8443/tcp
sudo firewall-cmd --permanent --add-port=5432/tcp
sudo firewall-cmd --permanent --add-port=9090/tcp
sudo firewall-cmd --reload

# Verificar
sudo firewall-cmd --list-ports
```

### Windows

```bash
# PowerShell como Administrador
New-NetFirewallRule -DisplayName "Musify-8080" -Direction Inbound -Port 8080 -Protocol TCP -Action Allow
New-NetFirewallRule -DisplayName "Musify-4200" -Direction Inbound -Port 4200 -Protocol TCP -Action Allow
New-NetFirewallRule -DisplayName "Musify-8443" -Direction Inbound -Port 8443 -Protocol TCP -Action Allow
```

## 📱 Acceso desde Dispositivos Móviles

### Configuración Adicional

1. **Asegurar que el firewall permita conexiones**
2. **Verificar que Docker exponga los puertos correctamente**
3. **Usar la IP local, no localhost**

```bash
# Verificar que los puertos estén abiertos desde otro dispositivo
# Desde otro equipo en la red:
curl http://TU_IP:8080/actuator/health
```

### URLs de Acceso Móvil

```
📱 Aplicación completa: http://TU_IP:8080
📱 Frontend desarrollo: http://TU_IP:4200
📱 API directa: https://TU_IP:8443
```

## 🔍 Verificación de Despliegue

### Script de Verificación

```bash
#!/bin/bash
# verify_deployment.sh

HOST=${1:-localhost}

echo "🔍 Verificando despliegue en $HOST..."

# Verificar servicios principales
curl -s "http://$HOST:8080/actuator/health" && echo "✅ NGINX + Backend OK"
curl -s "http://$HOST:4200" | grep -q "Musify" && echo "✅ Frontend OK"
curl -s -k "https://$HOST:8443/actuator/health" && echo "✅ Backend directo OK"
curl -s "http://$HOST:9090/stream" && echo "✅ Flaky service OK"

echo "🎯 Despliegue verificado en $HOST"
```

**Uso:**
```bash
chmod +x verify_deployment.sh
./verify_deployment.sh 192.168.1.100
```

### Verificación Manual

```bash
# Verificar que los contenedores estén ejecutándose
docker compose ps

# Verificar logs si hay problemas
docker compose logs nginx
docker compose logs backend-app-1
docker compose logs angular-frontend

# Verificar conectividad de red
docker network ls
docker network inspect musify_default
```

## 🚀 Script de Despliegue Automatizado

### deploy.sh

```bash
#!/bin/bash
set -e

echo "🚀 Desplegando Musify con acceso externo..."

# Detectar IP automáticamente
LOCAL_IP=$(ip route get 1.1.1.1 | awk '{print $7}' | head -n1)

echo "📡 IP detectada: $LOCAL_IP"

# Actualizar configuración si es necesario
echo "🔧 Configurando servicios..."

# Desplegar con Docker Compose
echo "🐳 Iniciando contenedores..."
docker compose up --build -d

# Esperar a que los servicios estén listos
echo "⏳ Esperando a que los servicios estén listos..."
sleep 30

# Verificar salud
echo "🔍 Verificando servicios..."
curl -s "http://localhost:8080/actuator/health" > /dev/null && echo "✅ Servicio principal OK"

echo ""
echo "🎉 ¡Despliegue completado!"
echo ""
echo "📱 URLs de acceso:"
echo "   Aplicación principal: http://$LOCAL_IP:8080"
echo "   Frontend desarrollo:  http://$LOCAL_IP:4200"
echo "   API Backend:          https://$LOCAL_IP:8443"
echo ""
echo "🔧 Para acceso desde otros equipos, configura el firewall:"
echo "   sudo ufw allow 8080/tcp"
echo "   sudo ufw allow 4200/tcp"
echo "   sudo ufw allow 8443/tcp"
```

## 🔧 Troubleshooting

### Problemas Comunes

#### "No se puede acceder desde otro equipo"

```bash
# 1. Verificar que Docker exponga los puertos correctamente
docker compose ps

# 2. Verificar firewall
sudo ufw status

# 3. Verificar que los servicios estén ejecutándose
curl localhost:8080/actuator/health

# 4. Verificar logs
docker compose logs nginx
```

#### "Frontend no carga desde IP externa"

```bash
# Verificar configuración de environment
cat frontend/MusifyFront/src/environments/environment.ts

# Reconstruir frontend si es necesario
docker compose up --build angular-frontend
```

#### "Backend SSL/TLS errors"

```bash
# Para desarrollo, usar HTTP en lugar de HTTPS
curl http://TU_IP:8080/actuator/health  # A través de NGINX

# O ignorar certificados SSL
curl -k https://TU_IP:8443/actuator/health  # Directo al backend
```

#### "Base de datos no accesible"

```bash
# Verificar que PostgreSQL esté ejecutándose
docker compose logs postgres

# Probar conexión
psql -h TU_IP -p 5432 -U musify_user -d musify_db

# Variables de entorno
docker compose exec postgres env | grep POSTGRES
```

## 📊 Monitoreo de Red

### Verificar Conexiones Activas

```bash
# Ver conexiones por puerto
sudo netstat -tlnp | grep :8080
sudo netstat -tlnp | grep :4200
sudo netstat -tlnp | grep :8443

# Ver conexiones de Docker
docker compose exec nginx netstat -tlnp
```

### Logs de Acceso

```bash
# Logs de NGINX (incluye IPs de clientes)
docker compose logs nginx | grep "GET\|POST"

# Logs del backend
docker compose logs backend-app-1 | grep "HTTP"

# Logs en tiempo real
docker compose logs -f nginx
```

## 🌐 Configuración para Producción

### Reverse Proxy Adicional (Opcional)

Para producción, considera usar un reverse proxy adicional:

```nginx
# /etc/nginx/sites-available/musify
server {
    listen 80;
    server_name tu-dominio.com;
    
    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

### SSL/HTTPS (Producción)

```bash
# Obtener certificado SSL con Let's Encrypt
sudo certbot --nginx -d tu-dominio.com

# Configurar renovación automática
sudo crontab -e
0 12 * * * /usr/bin/certbot renew --quiet
```

---

> **Nota**: Para desarrollo y testing, usa HTTP. Para producción, configura HTTPS apropiadamente con certificados válidos.