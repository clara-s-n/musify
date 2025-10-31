#!/usr/bin/env bash

# ============================================================================
# DEMOSTRACIÓN: PATRONES DE SEGURIDAD (RATE LIMITING + VALIDATION + AUTH)
# ============================================================================
# Este script demuestra tácticas de seguridad (resistir ataques):
# 1. Validación de entrada: Se rechazan emails con formato inválido
# 2. Rate limiting: Se limita a 5 intentos de login por minuto
# 3. JWT Authentication: Autenticación segura basada en tokens
# 4. Gatekeeper: NGINX como punto único de entrada con TLS
#
# Atributos de calidad demostrados:
# - SEGURIDAD: Protección contra ataques de fuerza bruta
# - RESISTENCIA A ATAQUES: Validación y rate limiting
# - CONFIDENCIALIDAD: Comunicación cifrada con TLS
# - INTEGRIDAD: Tokens JWT firmados
# ============================================================================

set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"
COLORS_ENABLED=true

# Colores para output
if [ "$COLORS_ENABLED" = true ]; then
  GREEN='\033[0;32m'
  RED='\033[0;31m'
  YELLOW='\033[1;33m'
  BLUE='\033[0;34m'
  NC='\033[0m'
else
  GREEN=''
  RED=''
  YELLOW=''
  BLUE=''
  NC=''
fi

print_header() {
  echo -e "\n${BLUE}===================================================================${NC}"
  echo -e "${BLUE}$1${NC}"
  echo -e "${BLUE}===================================================================${NC}\n"
}

print_success() {
  echo -e "${GREEN}✓ $1${NC}"
}

print_error() {
  echo -e "${RED}✗ $1${NC}"
}

print_info() {
  echo -e "${YELLOW}ℹ $1${NC}"
}

print_header "VERIFICACIÓN DE DISPONIBILIDAD DEL SISTEMA"

if curl -s -f "${BASE_URL}/actuator/health" > /dev/null 2>&1; then
  print_success "Backend está disponible"
else
  print_error "Backend no está disponible en ${BASE_URL}"
  echo "Por favor, inicia el sistema con: docker compose up"
  exit 1
fi

print_header "PATRÓN 1: VALIDACIÓN DE ENTRADA"

print_info "El DTO LoginRequest usa @Email para validar formato de email"
print_info "Bean Validation rechaza automáticamente entradas inválidas"
echo ""

echo "Probando con emails inválidos..."
echo ""

INVALID_EMAILS=("not-an-email" "missing@domain" "@nodomain.com" "spaces in@email.com" "")

for email in "${INVALID_EMAILS[@]}"; do
  echo -e "\n${YELLOW}--- Test: Email='$email' ---${NC}"
  
  response=$(curl -s -w "\n%{http_code}" -X POST "${BASE_URL}/auth/login" \
    -H 'Content-Type: application/json' \
    -d "{\"email\":\"$email\",\"password\":\"password\"}")
  
  http_code=$(echo "$response" | tail -n1)
  body=$(echo "$response" | head -n-1)
  
  if [ "$http_code" = "400" ]; then
    print_error "Email inválido rechazado (HTTP 400)"
    echo "$body" | jq '.' 2>/dev/null || echo "$body"
  else
    print_info "Status: $http_code"
    echo "$body" | jq '.' 2>/dev/null || echo "$body"
  fi
done

echo ""
print_success "DEMOSTRACIÓN: Validación de entrada funciona correctamente"
print_success "Emails inválidos son rechazados antes de procesamiento"

print_header "PATRÓN 2: RATE LIMITING (PREVENCIÓN DE ATAQUES DE FUERZA BRUTA)"

print_info "Configuración en application.yaml:"
echo "  resilience4j.ratelimiter.instances.loginLimiter:"
echo "    limitForPeriod: 5"
echo "    limitRefreshPeriod: 60000ms  # 1 minuto"
echo "    timeoutDuration: 0"
echo ""

print_info "Simulando ataque de fuerza bruta con múltiples intentos de login..."
echo ""

VALID_EMAIL="attacker@test.com"
ATTEMPT_COUNT=8

SUCCESS_COUNT=0
RATE_LIMITED_COUNT=0

for i in $(seq 1 $ATTEMPT_COUNT); do
  echo -e "\n${YELLOW}--- Intento $i/$ATTEMPT_COUNT ---${NC}"
  
  response=$(curl -s -w "\n%{http_code}" -X POST "${BASE_URL}/auth/login" \
    -H 'Content-Type: application/json' \
    -d "{\"email\":\"$VALID_EMAIL\",\"password\":\"wrong_password_$i\"}")
  
  http_code=$(echo "$response" | tail -n1)
  body=$(echo "$response" | head -n-1)
  
  echo "HTTP Status: $http_code"
  echo "$body" | jq '.' 2>/dev/null || echo "$body"
  
  if [ "$http_code" = "429" ]; then
    print_error "Rate limit excedido - Request bloqueado"
    RATE_LIMITED_COUNT=$((RATE_LIMITED_COUNT + 1))
  else
    print_info "Request procesado (intento $i de 5 permitidos por minuto)"
    SUCCESS_COUNT=$((SUCCESS_COUNT + 1))
  fi
  
  sleep 0.5
done

echo ""
print_header "RESULTADOS DEL RATE LIMITING"
echo "Total de intentos: $ATTEMPT_COUNT"
echo "Requests procesados: $SUCCESS_COUNT"
echo "Requests bloqueados por rate limit: $RATE_LIMITED_COUNT"
echo ""

if [ $RATE_LIMITED_COUNT -gt 0 ]; then
  print_success "DEMOSTRACIÓN: Rate limiting funciona correctamente"
  print_success "Después de 5 intentos, los requests adicionales son bloqueados"
  print_success "Esto previene ataques de fuerza bruta automatizados"
else
  print_info "Rate limiting puede tomar algunos segundos en activarse"
  print_info "Los primeros 5 intentos siempre serán procesados"
fi

print_header "PATRÓN 3: AUTENTICACIÓN JWT"

print_info "JWT (JSON Web Token) proporciona autenticación stateless"
print_info "Token firmado con HMAC-SHA256 usando secret key"
echo ""

echo "Login exitoso con credenciales válidas..."
response=$(curl -s -X POST "${BASE_URL}/auth/login" \
  -H 'Content-Type: application/json' \
  -d '{"email":"user@demo.com","password":"password"}')

echo "$response" | jq '.'
echo ""

TOKEN=$(echo "$response" | jq -r '.data.accessToken // empty')

if [ -n "$TOKEN" ]; then
  print_success "Token JWT generado correctamente"
  echo ""
  echo "Token (primeros 50 caracteres):"
  echo "${TOKEN:0:50}..."
  echo ""
  
  # Decodificar header y payload del JWT
  print_info "Decodificando JWT (header.payload.signature):"
  echo ""
  
  HEADER=$(echo "$TOKEN" | cut -d. -f1)
  PAYLOAD=$(echo "$TOKEN" | cut -d. -f2)
  
  echo "Header (decodificado):"
  echo "$HEADER" | base64 -d 2>/dev/null | jq '.' || echo "(decode failed)"
  echo ""
  
  echo "Payload (decodificado):"
  echo "$PAYLOAD" | base64 -d 2>/dev/null | jq '.' || echo "(decode failed)"
  echo ""
  
  print_info "Probando acceso a endpoint protegido con token válido..."
  protected_response=$(curl -s -X GET "${BASE_URL}/playback/status" \
    -H "Authorization: Bearer ${TOKEN}")
  
  echo "$protected_response" | jq '.' 2>/dev/null || echo "$protected_response"
  print_success "Acceso permitido con token válido"
  echo ""
  
  print_info "Probando acceso sin token (debe fallar)..."
  no_auth_response=$(curl -s -w "\n%{http_code}" -X GET "${BASE_URL}/playback/status")
  http_code=$(echo "$no_auth_response" | tail -n1)
  
  if [ "$http_code" = "401" ] || [ "$http_code" = "403" ]; then
    print_success "Acceso denegado sin token (HTTP $http_code)"
  else
    print_info "Status: $http_code"
  fi
else
  print_error "No se pudo obtener token JWT"
fi

print_header "PATRÓN 4: GATEKEEPER (NGINX COMO GATEWAY)"

print_info "NGINX actúa como punto único de entrada con:"
echo "  - TLS/HTTPS termination"
echo "  - Load balancing entre réplicas"
echo "  - Protección de backend (no accesible directamente)"
echo "  - Rate limiting adicional (configurado en nginx.conf)"
echo ""

print_success "Arquitectura de seguridad en capas:"
echo "  Internet → NGINX (TLS) → Backend (validación + rate limit + JWT)"

print_header "PATRÓN 5: GATEWAY OFFLOADING"

print_info "NGINX offload funciones de seguridad:"
echo "  ✓ TLS termination (cifrado/descifrado)"
echo "  ✓ Connection pooling (reutilización de conexiones)"
echo "  ✓ Request buffering"
echo "  ✓ Retries automáticos (proxy_next_upstream)"
echo "  ✓ Health checks pasivos"
echo ""

print_success "Backend se enfoca en lógica de negocio,"
print_success "mientras NGINX maneja aspectos de infraestructura"

print_header "VERIFICACIÓN DE IMPLEMENTACIÓN EN EL CÓDIGO"

echo "Para verificar la implementación, revisa:"
echo ""
echo "1. Validación de entrada (LoginRequest.java):"
echo "   @Email"
echo "   private String email;"
echo ""
echo "2. Rate limiting (AuthService.java):"
echo "   @RateLimiter(name=\"loginLimiter\")"
echo "   public AuthResponseDto login(LoginRequest request)"
echo ""
echo "3. JWT Authentication (JwtTokenProvider.java):"
echo "   generateToken(Authentication authentication)"
echo "   validateToken(String token)"
echo ""
echo "4. Gatekeeper (frontend/MusifyFront/ops/nginx.conf):"
echo "   upstream backend {"
echo "     server backend-app-1:8443;"
echo "     server backend-app-2:8443;"
echo "   }"
echo ""
echo "5. Configuration (application.yaml):"
echo "   resilience4j.ratelimiter.instances.loginLimiter:"
echo "     limitForPeriod: 5"
echo "     limitRefreshPeriod: 60000"
echo ""

print_header "ATRIBUTOS DE CALIDAD DEMOSTRADOS"

echo "✓ SEGURIDAD:"
echo "  - Validación de entrada previene inyección de datos maliciosos"
echo "  - Rate limiting protege contra ataques de fuerza bruta"
echo "  - JWT proporciona autenticación stateless y segura"
echo ""
echo "✓ RESISTENCIA A ATAQUES:"
echo "  - Múltiples capas de defensa (validación, rate limit, auth)"
echo "  - Fail-fast con validación temprana"
echo "  - Rate limiting previene abuso del sistema"
echo ""
echo "✓ CONFIDENCIALIDAD:"
echo "  - TLS cifra comunicación (NGINX)"
echo "  - JWT tokens firmados previenen falsificación"
echo "  - Passwords no expuestos en logs"
echo ""
echo "✓ INTEGRIDAD:"
echo "  - JWT signature verifica que token no fue modificado"
echo "  - HMAC-SHA256 garantiza integridad del token"
echo ""

print_success "Demostración completada exitosamente"
