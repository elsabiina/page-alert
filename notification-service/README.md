# Notification Service - Sistema de Notificaciones

Microservicio especializado en el envío de notificaciones del sistema Page Alert. Gestiona notificaciones por email, confirmación de cuentas, integración con Kafka para eventos del sistema y manejo de tokens de confirmación.

## 🏗️ Características

- **Envío de Emails**: Notificaciones por correo electrónico con plantillas HTML
- **Confirmación de cuentas**: Tokens de activación y verificación de email
- **Integración Kafka**: Consumo de eventos del sistema para notificaciones
- **Gestión de tokens**: Creación, validación y limpieza de tokens expirados
- **Push Notifications**: Soporte para notificaciones push (planificado)
- **Plantillas**: Sistema de plantillas para diferentes tipos de notificaciones

## ⚙️ Perfiles de Spring Boot

El servicio de notificaciones soporta 3 perfiles diferentes para distintos entornos:

### 🛠️ Perfil de Desarrollo (`dev`)
- **Base de datos**: H2 en memoria (tokens de confirmación)
- **Email**: Configuración SMTP de desarrollo (consola/archivo)
- **Kafka**: Conexión localhost para testing
- **Templates**: Plantillas simplificadas para desarrollo
- **Logging**: Nivel DEBUG para debugging de emails
- **Uso**: Desarrollo local rápido sin envío real de emails
```bash
# Perfil por defecto
./mvnw spring-boot:run

# Explícitamente
./mvnw spring-boot:run -Dspring.profiles.active=dev
```

### 🐳 Perfil Docker (`docker`)  
- **Base de datos**: PostgreSQL (contenedor notification-db)
- **Email**: Configuración SMTP real via contenedor
- **Kafka**: Conexión a contenedor Kafka (kafka:9092)
- **Templates**: Plantillas completas con estilos CSS
- **URLs**: Nombres de servicios Docker para comunicación
- **Uso**: Entorno containerizado con envío real de emails
```bash
export SPRING_PROFILES_ACTIVE=docker
docker-compose up --build
```

### 🏭 Perfil Producción (`prod`)
- **Base de datos**: PostgreSQL con optimizaciones de rendimiento
- **Email**: Configuración SMTP de producción (Gmail, SendGrid, etc.)
- **Pool de conexiones**: HikariCP optimizado para alta carga
- **Rate Limiting**: Control de velocidad de envío de emails
- **Security**: Configuración SSL/TLS para SMTP
- **Monitoring**: Métricas de emails enviados y fallos
- **Uso**: Despliegue en producción
```bash
export SPRING_PROFILES_ACTIVE=prod
./mvnw spring-boot:run
```

## 📊 Variables de Entorno

Cada perfil usa variables diferentes desde el archivo `.env`:

```env
# Database Configuration
DB_HOST=localhost           # docker: notification-db, prod: tu-host-prod
DB_PORT=5432
DB_NAME=pageAlert
DB_USERNAME=usuario
DB_PASSWORD=contraseña

# Email/SMTP Configuration
SPRING_MAIL_HOST=smtp.gmail.com         # Proveedor SMTP
SPRING_MAIL_PORT=587                    # Puerto SMTP
SPRING_MAIL_USERNAME=tu-email@gmail.com # Email de envío
SPRING_MAIL_PASSWORD=tu-app-password    # Contraseña/token app
SPRING_MAIL_SMTP_AUTH=true
SPRING_MAIL_SMTP_STARTTLS=true

# Application URLs
APP_FRONTEND_URL=http://localhost:5173  # URL del frontend
APP_CONFIRM_EMAIL_URL=${APP_FRONTEND_URL}/confirm-email

# Kafka Configuration
KAFKA_BOOTSTRAP_SERVERS=localhost:9092      # docker: kafka:9092
KAFKA_TOPIC_USER_EVENTS=user-events         # Topic de eventos de usuario
KAFKA_TOPIC_PAGE_CHANGES=page-changes       # Topic de cambios de páginas

# Token Configuration
TOKEN_EXPIRATION_HOURS=24               # Expiración tokens confirmación
TOKEN_CLEANUP_INTERVAL=3600000          # Limpieza automática (1h)

# Logging
LOGGING_LEVEL=INFO          # dev: DEBUG, docker: INFO, prod: WARN
```

## 🛠️ Instalación y Ejecución

### Desarrollo Local (H2)
```bash
cd notification-service
# Configurar variables en .env
./mvnw spring-boot:run
# Accede a: http://localhost:4020
# Swagger: http://localhost:4020/swagger-ui/index.html
```

### Docker (PostgreSQL)
```bash
cd notification-service
docker-compose up --build
# O desde la raíz: docker-compose up notification-service
```

### Producción
```bash
export SPRING_PROFILES_ACTIVE=prod
# Configurar todas las variables de entorno de producción
./mvnw spring-boot:run
```

## 📊 Endpoints API

### GET /notifications/confirm-email/{token}
Confirma una dirección de email con token
```bash
curl http://localhost:4020/notifications/confirm-email/abc123-def456-ghi789
```

### POST /notifications/cleanup-expired-tokens
Limpia tokens expirados manualmente
```bash
curl -X POST http://localhost:4020/notifications/cleanup-expired-tokens
```

## 📧 Sistema de Email

### Configuración SMTP
```yaml
# application-prod.yml
spring:
  mail:
    host: ${SPRING_MAIL_HOST}
    port: ${SPRING_MAIL_PORT}
    username: ${SPRING_MAIL_USERNAME}
    password: ${SPRING_MAIL_PASSWORD}
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
          ssl:
            trust: ${SPRING_MAIL_HOST}
```

### Plantillas de Email
```html
<!-- confirm-email.html -->
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Confirma tu Email - Page Alert</title>
</head>
<body>
    <div class="email-container">
        <h1>¡Bienvenido a Page Alert!</h1>
        <p>Haz clic en el enlace para confirmar tu email:</p>
        <a href="{{confirmUrl}}" class="confirm-button">Confirmar Email</a>
        <p>Este enlace expira en 24 horas.</p>
    </div>
</body>
</html>
```

### Tipos de Notificaciones
```java
public enum NotificationType {
    EMAIL_CONFIRMATION,     // Confirmación de registro
    PAGE_CHANGE_ALERT,      // Cambio detectado en página
    WEEKLY_SUMMARY,         // Resumen semanal
    ACCOUNT_SECURITY,       // Alertas de seguridad
    SYSTEM_MAINTENANCE      // Mantenimiento del sistema
}
```

## 🎯 Integración con Kafka

### Consumer de eventos
```java
@KafkaListener(topics = "user-events")
public void handleUserEvent(UserEvent event) {
    switch(event.getType()) {
        case USER_REGISTERED:
            sendEmailConfirmation(event.getUser());
            break;
        case PASSWORD_CHANGED:
            sendSecurityAlert(event.getUser());
            break;
    }
}

@KafkaListener(topics = "page-changes")
public void handlePageChange(PageChangeEvent event) {
    sendPageChangeNotification(event);
}
```

### Eventos Kafka consumidos
```json
{
  "type": "USER_REGISTERED",
  "userId": "user123",
  "email": "user@example.com",
  "timestamp": "2024-01-15T10:30:00Z",
  "data": {
    "name": "Juan Pérez",
    "registrationSource": "web"
  }
}
```

## 🔧 Servicios Principales

### NotificationService
```java
@Service
public class NotificationService {
    
    // Envía email de confirmación
    public void sendEmailConfirmation(User user);
    
    // Confirma email con token
    public void confirmEmail(String token);
    
    // Envía notificación de cambio
    public void sendPageChangeNotification(PageChangeEvent event);
    
    // Limpia tokens expirados
    public void cleanupExpiredTokens();
}
```

### EmailTemplateService
```java
@Service 
public class EmailTemplateService {
    
    // Procesa plantilla con variables
    public String processTemplate(String template, Map<String, Object> variables);
    
    // Carga plantilla desde recursos
    public String loadTemplate(NotificationType type);
}
```

## 🧪 Testing

### Verificar servicio
```bash
curl http://localhost:4020/actuator/health
```

### Test de confirmación email
```bash
# Simular registro de usuario (vía Kafka o directamente)
curl -X POST http://localhost:4005/register \
  -H "Content-Type: application/json" \
  -d '{"name": "Test User", "email": "test@example.com", "password": "password123"}'

# Verificar que se recibió el email de confirmación
# Usar token recibido para confirmar
curl http://localhost:4020/notifications/confirm-email/TOKEN_RECIBIDO
```

### Test limpieza de tokens
```bash
curl -X POST http://localhost:4020/notifications/cleanup-expired-tokens
```

## 📈 Monitoreo y Métricas

### Métricas importantes
- **Emails enviados por día/hora**
- **Tasa de confirmación de emails**
- **Tokens expirados sin usar**
- **Fallos de envío SMTP**
- **Tiempo promedio de procesamiento**

### Health Check personalizado
```java
@Component
public class EmailHealthIndicator implements HealthIndicator {
    
    @Override
    public Health health() {
        try {
            // Test conexión SMTP
            mailSender.testConnection();
            return Health.up()
                .withDetail("smtp", "connected")
                .build();
        } catch (Exception e) {
            return Health.down()
                .withDetail("smtp", "disconnected")
                .withDetail("error", e.getMessage())
                .build();
        }
    }
}
```

## 🔒 Configuración de Seguridad

### Tokens seguros
```java
@Component
public class TokenGenerator {
    
    public String generateConfirmationToken() {
        return UUID.randomUUID().toString() + "-" + 
               System.currentTimeMillis() + "-" +
               UUID.randomUUID().toString().substring(0, 8);
    }
}
```

### Rate Limiting para emails
```yaml
# application-prod.yml
notification:
  email:
    rate-limit:
      max-per-user-per-hour: 5
      max-per-ip-per-hour: 20
      max-global-per-minute: 100
```

## 📝 Logging y Debugging

### Configuración logging
```yaml
# application-dev.yml
logging:
  level:
    es.oscasais.pa.notification: DEBUG
    org.springframework.mail: DEBUG
    org.springframework.kafka: INFO
  pattern:
    console: "%d{HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
```

### Logs importantes
```java
log.info("Sending email confirmation to user: {}", user.getEmail());
log.debug("Email sent successfully to: {} with token: {}", email, token);
log.warn("Failed to send email to: {} - Reason: {}", email, exception.getMessage());
log.error("SMTP connection failed: {}", exception.getMessage());
```

## 🚨 Troubleshooting

### Error: "Failed to connect to SMTP server"
- **Causa**: Configuración SMTP incorrecta o credenciales inválidas
- **Solución**: Verificar `SPRING_MAIL_*` variables y conectividad

### Error: "Invalid or expired confirmation token"
- **Causa**: Token usado, expirado o malformado
- **Solución**: Generar nuevo token o verificar base de datos

### Error: "Template not found"
- **Causa**: Archivo de plantilla faltante en resources/templates
- **Solución**: Verificar que existe el archivo de plantilla

### Error: "Too many emails sent"
- **Causa**: Rate limiting activado
- **Solución**: Esperar o ajustar configuración de límites

### Error: "Kafka consumer lag"
- **Causa**: Procesamiento lento de eventos
- **Solución**: Escalar instancias o optimizar procesamiento

## 🔄 Configuración de Proveedores Email

### Gmail (Desarrollo)
```env
SPRING_MAIL_HOST=smtp.gmail.com
SPRING_MAIL_PORT=587
SPRING_MAIL_USERNAME=tu-email@gmail.com
SPRING_MAIL_PASSWORD=tu-app-password  # No tu password normal
```

### SendGrid (Producción)
```env
SPRING_MAIL_HOST=smtp.sendgrid.net
SPRING_MAIL_PORT=587
SPRING_MAIL_USERNAME=apikey
SPRING_MAIL_PASSWORD=tu-sendgrid-api-key
```