# API Guard - Gateway Service

API Gateway que actúa como punto de entrada único para todos los servicios del sistema Page Alert. Implementa autenticación basada en JWT mediante cookies HTTP-only y enrutamiento a microservicios.

## 🏗️ Arquitectura

Este servicio implementa el patrón **API Gateway** utilizando Spring Cloud Gateway, proporcionando:

- **Enrutamiento centralizado** a microservicios
- **Autenticación JWT** mediante cookies seguras
- **Configuración CORS** para aplicaciones frontend
- **Filtrado de seguridad** en todas las peticiones

## 🔐 Flujo de Autenticación

### 1. Proceso de Login
```
Frontend → API Gateway → Auth Service
    ↓
Cookie JWT ← API Gateway ← Auth Service
```

1. **Frontend** envía credenciales a `/api/auth/login`
2. **API Gateway** enruta la petición al **Auth Service** 
3. **Auth Service** valida credenciales y genera JWT
4. **Auth Service** devuelve JWT en cookie HTTP-only, Secure
5. **API Gateway** retorna cookie al frontend

### 2. Peticiones Autenticadas
```
Frontend (con cookie) → API Gateway → Validación JWT → Microservicio
```

1. **Frontend** envía petición con cookie JWT automáticamente
2. **API Gateway** intercepta con `CookieJwtValidationGatewayFilterFactory`
3. **Filtro JWT** extrae token de la cookie `jwtToken`
4. **Validación JWT** verifica firma y expiración
5. Si es válido → enruta a microservicio de destino
6. Si es inválido → retorna `401 Unauthorized`

### 3. Configuración CORS
```
OPTIONS Request → CORS Filter → 200 OK (headers CORS)
POST/GET Request → CORS Filter → JWT Filter → Microservicio
```

- **Preflight OPTIONS** se procesa sin autenticación
- **CORS habilitado** para `http://localhost:5173` (desarrollo)
- **Cookies permitidas** con `Access-Control-Allow-Credentials: true`

## 🚀 Configuración

### Puertos
- **Gateway**: `4010`
- **Auth Service**: `4005`
- **Frontend**: `5173`

### Variables de Entorno
```yaml
JWT_SECRET: "clave-secreta-para-firmar-jwt"
JWT_ISSUER: "page-alert-auth"
COOKIE_DOMAIN: "localhost"  # Producción: tu dominio
```

### Rutas Configuradas
```yaml
routes:
  - id: auth-service
    uri: lb://auth-service
    predicates:
      - Path=/api/auth/**
    filters:
      - RewritePath=/api/auth/(?<segment>.*), /${segment}
```

## 🔧 Componentes Clave

### 1. CookieJwtValidationGatewayFilterFactory
- **Ubicación**: `src/main/java/es/oscasais/pa/apiguard/filter/`
- **Función**: Intercepta peticiones y valida JWT de cookies
- **Excluye**: Peticiones OPTIONS (CORS preflight)
- **Extrae**: Token de cookie `jwtToken`

### 2. CorsConfiguration
- **Ubicación**: `src/main/java/es/oscasais/pa/apiguard/config/`
- **Función**: Configura CORS para aplicaciones SPA
- **Prioridad**: `HIGHEST_PRECEDENCE` (ejecuta antes que otros filtros)

### 3. JwtValidationException
- **Ubicación**: `src/main/java/es/oscasais/pa/apiguard/exception/`
- **Función**: Manejo de errores de validación JWT

## ⚙️ Perfiles de Spring Boot

El API Gateway soporta 3 perfiles diferentes para distintos entornos:

### 🛠️ Perfil de Desarrollo (`dev`)
- **Base de datos**: H2 en memoria (sin instalación)
- **URLs de servicios**: localhost para comunicación interna
- **Uso**: Desarrollo local rápido
```bash
# Perfil por defecto
./mvnw spring-boot:run

# Explícitamente
./mvnw spring-boot:run -Dspring.profiles.active=dev
```

### 🐳 Perfil Docker (`docker`)  
- **Base de datos**: PostgreSQL (contenedores)
- **URLs de servicios**: Nombres de servicios Docker para networking
- **Uso**: Entorno containerizado
```bash
export SPRING_PROFILES_ACTIVE=docker
docker-compose up --build
```

### 🏭 Perfil Producción (`prod`)
- **Base de datos**: PostgreSQL optimizado 
- **Configuración**: Pool de conexiones, logging mínimo, seguridad reforzada
- **Uso**: Despliegue en producción
```bash
export SPRING_PROFILES_ACTIVE=prod
./mvnw spring-boot:run
```

**Configuración de perfiles:**
- Las variables de entorno se leen desde `.env`
- Cada perfil tiene su archivo `application-{profile}.yml`
- El perfil por defecto es `dev` para desarrollo local

## 🛠️ Instalación

### Docker Build
```bash
cd api-guard/
docker build -t api-guard .
```

### Ejecución Local
```bash
./mvnw spring-boot:run
```

### Docker Compose
```bash
# Desde directorio raíz del proyecto
docker-compose up api-guard
```

## 🧪 Testing

### Verificar CORS
```bash
curl -X OPTIONS \
  -H "Origin: http://localhost:5173" \
  -H "Access-Control-Request-Method: POST" \
  -H "Access-Control-Request-Headers: Content-Type" \
  -v http://localhost:4010/api/auth/login
```

### Test de Autenticación
```bash
# 1. Login (obtienes cookie)
curl -X POST http://localhost:4010/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password123"}' \
  -c cookies.txt

# 2. Petición autenticada (usando cookie)
curl -X GET http://localhost:4010/api/protected-endpoint \
  -b cookies.txt
```

## 📝 Logs de Debug

Para debug detallado, activar en `application.yml`:
```yaml
logging:
  level:
    es.oscasais.pa.apiguard: DEBUG
    org.springframework.cloud.gateway: DEBUG
```

## 🔒 Seguridad

### JWT Validation
- **Algoritmo**: HS256
- **Verificación**: Firma + Expiración + Issuer
- **Cookie Attributes**: HttpOnly, Secure, SameSite

### CORS Policy
- **Origins**: Whitelist específica (no wildcard en producción)
- **Credentials**: Habilitado para cookies
- **Methods**: GET, POST, PUT, DELETE, OPTIONS
- **Headers**: Content-Type, Authorization, X-Requested-With

## 🚨 Troubleshooting

### Error: "OPTIONS Forbidden"
- **Causa**: CORS filter no ejecutándose antes que JWT filter
- **Solución**: Verificar `@Order(Ordered.HIGHEST_PRECEDENCE)` en CorsConfiguration

### Error: "JWT Cookie Not Found"
- **Causa**: Cookie `jwtToken` no presente en petición
- **Solución**: Verificar que frontend incluye cookies automáticamente

### Error: "CORS Origin Not Allowed"
- **Causa**: Origin del frontend no está en whitelist
- **Solución**: Añadir origin en `CorsConfiguration.allowedOriginPatterns`
