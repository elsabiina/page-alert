# Auth Service - Servicio de Autenticación

Microservicio especializado en autenticación de usuarios del sistema Page Alert. Genera y valida tokens JWT, gestiona cookies seguras y proporciona endpoints de login/logout.

## 🏗️ Arquitectura

Este servicio implementa el patrón **Authentication Service** utilizando Spring Boot, proporcionando:

- **Autenticación JWT** con tokens firmados y cookies HTTP-only
- **Gestión de usuarios** con validación y persistencia
- **Cookies seguras** con configuración HttpOnly, Secure, SameSite
- **Validación robusta** de credenciales con BCrypt

## 🔐 Flujo de Autenticación

### 1. Proceso de Login
```
Frontend → API Gateway → Auth Service
                            ↓
                       [Validate Credentials]
                            ↓
                       [Generate JWT Token]
                            ↓
                       [Create Secure Cookie]
                            ↓
Frontend ← API Gateway ← Set-Cookie: jwtToken
```

#### Pasos Detallados:
1. **Frontend** envía `POST /login` con `{email, password}`
2. **AuthController** recibe `LoginRequestDTO`
3. **UserService** valida credenciales contra base de datos
4. **BCrypt** verifica hash de contraseña
5. **JwtUtil** genera token JWT con claims del usuario
6. **CookieService** crea cookie HTTP-only con el JWT
7. **Respuesta** incluye `Set-Cookie` header con token seguro

### 2. Estructura del JWT
```json
{
  "header": {
    "alg": "HS256",
    "typ": "JWT"
  },
  "payload": {
    "sub": "user@example.com",
    "iss": "page-alert-auth",
    "exp": 1640995200,
    "iat": 1640908800,
    "userId": 123,
    "email": "user@example.com"
  }
}
```

### 3. Configuración de Cookies
```http
Set-Cookie: jwtToken=eyJhbGciOiJIUzI1NiJ9...; 
           HttpOnly; 
           Secure; 
           SameSite=Lax; 
           Path=/; 
           Domain=localhost; 
           Max-Age=86400
```

## 🚀 Configuración

### Puerto
- **Auth Service**: `4005`

### Variables de Entorno Requeridas
```env
# JWT Configuration
JWT_SECRET=tu-clave-secreta-super-segura-256-bits
JWT_ISSUER=page-alert-auth
JWT_EXPIRATION_MS=86400000  # 24 horas

# Cookie Configuration  
COOKIE_DOMAIN=localhost     # Producción: tu-dominio.com
COOKIE_SECURE=false        # Producción: true
COOKIE_SAME_SITE=Lax       # Strict|Lax|None

# Database
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/pageAlert
SPRING_DATASOURCE_USERNAME=usuario
SPRING_DATASOURCE_PASSWORD=contraseña
```

### Base de Datos
```sql
-- Tabla Users (ejemplo)
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,  -- BCrypt hash
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## 🔧 Componentes Clave

### 1. AuthController
- **Ubicación**: `src/main/java/es/oscasais/pa/auth/controller/AuthController.java`
- **Endpoints**:
  - `POST /login` - Autenticación de usuario
  - `POST /logout` - Limpieza de cookie JWT
  - `GET /validate` - Validación de token JWT
- **Validaciones**: `@Valid LoginRequestDTO` con Bean Validation

### 2. JwtUtil
- **Ubicación**: `src/main/java/es/oscasais/pa/auth/util/JwtUtil.java`  
- **Funciones**:
  - `generateToken(User user)` - Genera JWT con claims
  - `generateToken(String email)` - Genera JWT básico
  - `extractEmail(String token)` - Extrae email del JWT
  - `validateToken(String token)` - Valida firma y expiración
- **Algoritmo**: HS256 con clave secreta configurada

### 3. CookieService
- **Ubicación**: `src/main/java/es/oscasais/pa/auth/service/CookieService.java`
- **Funciones**:
  - `createJwtCookie(String token)` - Crea cookie segura
  - `createLogoutCookie()` - Cookie para logout (Max-Age=0)
- **Configuración**: HttpOnly, Secure (prod), SameSite, Domain

### 4. LoginRequestDTO
- **Ubicación**: `src/main/java/es/oscasais/pa/auth/dto/LoginRequestDTO.java`
- **Validaciones**:
  - `@NotBlank @Email` para email
  - `@NotBlank @Size(min=8)` para password
- **Constructores**: Default (JSON) + Parameterizado

### 5. SecurityConfig
- **Ubicación**: `src/main/java/es/oscasais/pa/auth/config/SecurityConfig.java`
- **Configuración**:
  - CSRF deshabilitado (stateless JWT)
  - BCryptPasswordEncoder para hashing
  - Todas las rutas permitidas (auth interno)

## ⚙️ Perfiles de Spring Boot

El servicio de autenticación soporta 3 perfiles diferentes para distintos entornos:

### 🛠️ Perfil de Desarrollo (`dev`)
- **Base de datos**: H2 en memoria (automática, sin configuración)
- **URLs de servicios**: localhost para comunicación interna
- **JWT Secret**: Valor por defecto (solo desarrollo)
- **Logging**: Nivel DEBUG habilitado
- **Uso**: Desarrollo local rápido
```bash
# Perfil por defecto
./mvnw spring-boot:run

# Explícitamente
./mvnw spring-boot:run -Dspring.profiles.active=dev
```

### 🐳 Perfil Docker (`docker`)  
- **Base de datos**: PostgreSQL (contenedor auth-db)
- **URLs de servicios**: Nombres de servicios Docker (auth-service, user-service, etc.)
- **JWT Secret**: Variable de entorno `JWT_SECRET` requerida
- **Uso**: Entorno containerizado
```bash
export SPRING_PROFILES_ACTIVE=docker
docker-compose up --build
```

### 🏭 Perfil Producción (`prod`)
- **Base de datos**: PostgreSQL con optimizaciones de rendimiento
- **Pool de conexiones**: HikariCP configurado para alta carga
- **JWT Secret**: Variable de entorno `JWT_SECRET` obligatoria (min 256 bits)
- **Logging**: Nivel WARN, sin debug information
- **Cookies**: Secure=true, configuración HTTPS
- **Uso**: Despliegue en producción
```bash
export SPRING_PROFILES_ACTIVE=prod
export JWT_SECRET="tu-clave-super-segura-256-bits"
./mvnw spring-boot:run
```

**Variables de entorno por perfil:**
- **dev**: Variables opcionales (valores por defecto)
- **docker**: Variables desde `.env` + contenedor PostgreSQL
- **prod**: Variables obligatorias + configuración segura

## 🛠️ Instalación

### Requisitos
- Java 21
- Maven 3.9+
- PostgreSQL 15+ (o H2 para desarrollo)

### Docker Build
```bash
cd auth-service/
docker build -t auth-service .
```

### Ejecución Local
```bash
# Con Maven
./mvnw spring-boot:run

# Con Java directo
./mvnw clean package
java -jar target/auth-service-0.0.1-SNAPSHOT.jar

# Con variables de entorno
JWT_SECRET=mi-secreto-super-seguro ./mvnw spring-boot:run
```

### Docker Compose
```bash
# Desde directorio raíz del proyecto
docker-compose up auth-service
```

## 🧪 Testing

### Test de Login
```bash
# Login exitoso
curl -X POST http://localhost:4005/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@example.com",
    "password": "password123"
  }' \
  -v

# Respuesta esperada:
# Set-Cookie: jwtToken=eyJhbGciOiJIUzI1NiJ9...
# Status: 200 OK
```

### Test de Validación JWT
```bash
# Con cookie obtenida del login
curl -X GET http://localhost:4005/validate \
  -H "Cookie: jwtToken=eyJhbGciOiJIUzI1NiJ9..." \
  -v

# Respuesta esperada: 200 OK + user info
```

### Test de Logout
```bash
curl -X POST http://localhost:4005/logout \
  -H "Cookie: jwtToken=eyJhbGciOiJIUzI1NiJ9..." \
  -v

# Respuesta esperada:
# Set-Cookie: jwtToken=; Max-Age=0; HttpOnly
```

## 📊 Endpoints API

### POST /login
**Request:**
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

**Response (Success - 200):**
```http
HTTP/1.1 200 OK
Set-Cookie: jwtToken=eyJhbGciOiJIUzI1NiJ9...; HttpOnly; Secure; SameSite=Lax
Content-Type: application/json

{
  "message": "Login successful",
  "userId": 123
}
```

**Response (Error - 401):**
```json
{
  "error": "Invalid credentials",
  "timestamp": "2024-01-15T10:30:00Z"
}
```

### POST /logout
**Response:**
```http
HTTP/1.1 200 OK
Set-Cookie: jwtToken=; Max-Age=0; HttpOnly; Secure
Content-Type: application/json

{
  "message": "Logout successful"
}
```

### GET /validate
**Headers Required:**
```http
Cookie: jwtToken=eyJhbGciOiJIUzI1NiJ9...
```

**Response (Valid - 200):**
```json
{
  "valid": true,
  "userId": 123,
  "email": "user@example.com",
  "exp": 1640995200
}
```

## 🔒 Seguridad

### Configuración JWT
- **Algoritmo**: HS256 (HMAC con SHA-256)
- **Clave**: Mínimo 256 bits, configurable via `JWT_SECRET`
- **Expiración**: 24 horas por defecto (`JWT_EXPIRATION_MS`)
- **Claims**: sub, iss, exp, iat, userId, email

### Password Hashing
- **Algoritmo**: BCrypt con salt automático
- **Rounds**: 12 (configuración Spring Security por defecto)
- **Storage**: Solo hash almacenado, nunca password plano

### Cookie Security
- **HttpOnly**: ✅ Previene acceso via JavaScript
- **Secure**: ✅ Solo HTTPS (producción)
- **SameSite**: ✅ Lax (protección CSRF)
- **Domain**: ✅ Configurado para tu dominio
- **Path**: ✅ / (disponible en toda la app)

## 📝 Logs de Debug

Activar en `application.yml`:
```yaml
logging:
  level:
    es.oscasais.pa.auth: DEBUG
    org.springframework.security: DEBUG
    io.jsonwebtoken: DEBUG
```

## 🚨 Troubleshooting

### Error: "JWT Secret not configured"
- **Causa**: Variable `JWT_SECRET` no definida
- **Solución**: `export JWT_SECRET="tu-clave-secreta-256-bits"`

### Error: "Invalid credentials"
- **Causa**: Email/password incorrectos o usuario no existe
- **Solución**: Verificar datos en base de datos

### Error: "JWT signature does not match"
- **Causa**: `JWT_SECRET` diferente entre generación y validación
- **Solución**: Sincronizar secreto entre auth-service y api-guard

### Error: "Cookie not set in response"
- **Causa**: Configuración de cookie fallando
- **Solución**: Verificar `COOKIE_DOMAIN` y `COOKIE_SECURE`

## 🔄 Integración con API Gateway

### Flujo Completo:
1. **Frontend** → `POST http://localhost:4010/api/auth/login`
2. **API Gateway** → `POST http://auth-service:4005/login`
3. **Auth Service** → Valida y retorna cookie
4. **API Gateway** → Reenvía cookie al frontend
5. **Frontend** → Futuras peticiones incluyen cookie automáticamente
6. **API Gateway** → Valida JWT de cookie antes de enrutar

### Variables Compartidas:
```env
# Deben ser IDÉNTICAS en ambos servicios
JWT_SECRET=misma-clave-en-auth-y-gateway
JWT_ISSUER=page-alert-auth
COOKIE_DOMAIN=localhost  # o tu dominio
```