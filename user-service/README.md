# User Service - Gestión de Usuarios

Microservicio especializado en la gestión de usuarios del sistema Page Alert. Proporciona operaciones CRUD de usuarios, validaciones y persistencia usando Spring Boot Data JPA.

## 🏗️ Características

- **Gestión de usuarios**: Registro, actualización, eliminación y consulta
- **Validaciones**: Bean Validation para datos de entrada
- **Persistencia**: JPA/Hibernate con PostgreSQL/H2
- **APIs RESTful**: Endpoints completos para operaciones de usuario
- **Integración**: Comunicación con otros microservicios via HTTP

## ⚙️ Perfiles de Spring Boot

El servicio de usuarios soporta 3 perfiles diferentes para distintos entornos:

### 🛠️ Perfil de Desarrollo (`dev`)
- **Base de datos**: H2 en memoria (automática, sin configuración)
- **DDL**: `create-drop` para recrear esquema en cada inicio
- **Logging**: Nivel DEBUG para SQL queries
- **Datos**: Script `data.sql` se ejecuta automáticamente
- **Uso**: Desarrollo local rápido con datos de prueba
```bash
# Perfil por defecto
./mvnw spring-boot:run

# Explícitamente
./mvnw spring-boot:run -Dspring.profiles.active=dev
```

### 🐳 Perfil Docker (`docker`)  
- **Base de datos**: PostgreSQL (contenedor user-db)
- **DDL**: `update` para mantener datos entre reinicios
- **Pool de conexiones**: Configurado para contenedores
- **URLs**: Nombres de servicios Docker para comunicación
- **Uso**: Entorno containerizado
```bash
export SPRING_PROFILES_ACTIVE=docker
docker-compose up --build
```

### 🏭 Perfil Producción (`prod`)
- **Base de datos**: PostgreSQL con optimizaciones de rendimiento
- **DDL**: `validate` - solo valida esquema, no modifica
- **Pool de conexiones**: HikariCP optimizado para alta carga
- **Logging**: Nivel WARN, sin información sensible
- **Cache**: Configuraciones de cache habilitadas
- **Uso**: Despliegue en producción
```bash
export SPRING_PROFILES_ACTIVE=prod
./mvnw spring-boot:run
```

## 📊 Variables de Entorno

Cada perfil usa variables diferentes desde el archivo `.env`:

```env
# Database Configuration
DB_HOST=localhost           # docker: user-db, prod: tu-host-prod
DB_PORT=5432
DB_NAME=pageAlert
DB_USERNAME=usuario
DB_PASSWORD=contraseña

# JPA Configuration
JPA_DDL_AUTO=create-drop    # dev: create-drop, docker: update, prod: validate
JPA_SHOW_SQL=true           # dev: true, docker: false, prod: false

# Application Configuration
LOGGING_LEVEL=INFO          # dev: DEBUG, docker: INFO, prod: WARN
```

## 🛠️ Instalación y Ejecución

### Desarrollo Local (H2)
```bash
cd user-service
./mvnw spring-boot:run
# Accede a: http://localhost:4000
```

### Docker (PostgreSQL)
```bash
cd user-service
docker-compose up --build
# O desde la raíz: docker-compose up user-service
```

### Producción
```bash
export SPRING_PROFILES_ACTIVE=prod
# Configurar todas las variables de entorno de producción
./mvnw spring-boot:run
```

## 📊 Endpoints API

### GET /users
Lista todos los usuarios
```bash
curl http://localhost:4000/users
```

### GET /users/{id}
Obtiene un usuario por ID
```bash
curl http://localhost:4000/users/1
```

### POST /users
Crea un nuevo usuario
```bash
curl -X POST http://localhost:4000/users \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Juan Pérez",
    "email": "juan@example.com"
  }'
```

### PUT /users/{id}
Actualiza un usuario existente
```bash
curl -X PUT http://localhost:4000/users/1 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Juan Carlos",
    "email": "juan.carlos@example.com"
  }'
```

### DELETE /users/{id}
Elimina un usuario
```bash
curl -X DELETE http://localhost:4000/users/1
```

## 🗃️ Configuración de Base de Datos

### H2 (Desarrollo)
- **URL**: `jdbc:h2:mem:testdb`
- **Console**: `http://localhost:4000/h2-console`
- **Usuario**: `sa`, **Password**: *(vacío)*

### PostgreSQL (Docker/Prod)
```sql
-- Tabla Users (ejemplo)
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## 🧪 Testing

### Verificar conexión
```bash
curl http://localhost:4000/actuator/health
```

### Testing con datos
```bash
# Crear usuario
curl -X POST http://localhost:4000/users \
  -H "Content-Type: application/json" \
  -d '{"name": "Test User", "email": "test@example.com"}'

# Listar usuarios
curl http://localhost:4000/users
```

## 🔧 Configuraciones Adicionales

### Actuator (Monitoreo)
- **Health**: `/actuator/health`
- **Info**: `/actuator/info`
- **Metrics**: `/actuator/metrics`

### Validaciones
- **Bean Validation**: `@Valid`, `@NotNull`, `@Email`
- **Custom Validators**: Para reglas de negocio específicas

### Logging
```yaml
# application-dev.yml
logging:
  level:
    es.oscasais.pa.userservice: DEBUG
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
```

## 🚨 Troubleshooting

### Error: "Cannot create table users"
- **Causa**: Problema de permisos en PostgreSQL
- **Solución**: Verificar credenciales y permisos de BD

### Error: "Connection refused to database"
- **Causa**: PostgreSQL no iniciado o puerto incorrecto
- **Solución**: `docker-compose up postgresql` o verificar puerto

### Error: "Bean validation failed"
- **Causa**: Datos de entrada no válidos
- **Solución**: Verificar formato email, campos requeridos, etc.
