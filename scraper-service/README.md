# Scraper Service - Web Scraping y Monitoreo

Microservicio especializado en web scraping y monitoreo de URLs del sistema Page Alert. Proporciona detección de cambios en páginas web, WebSockets para comunicación en tiempo real y API para gestión de URLs monitorizadas.

## 🏗️ Características

- **Web Scraping**: Monitoreo automático de cambios en páginas web
- **WebSockets**: Comunicación en tiempo real con frontend para notificaciones
- **CRON Jobs**: Ejecución programada de tareas de scraping
- **APIs RESTful**: Gestión de URLs monitorizadas por usuario
- **Detección de cambios**: Comparación inteligente de contenido web
- **Integración Kafka**: Envío de eventos de cambios detectados

## ⚙️ Perfiles de Spring Boot

El servicio de scraping soporta 3 perfiles diferentes para distintos entornos:

### 🛠️ Perfil de Desarrollo (`dev`)
- **Base de datos**: H2 en memoria (automática, sin configuración)
- **Scraper API**: URL localhost (http://localhost:5000)
- **WebSocket**: Configuración local para desarrollo
- **CRON**: Intervalos cortos para testing
- **Logging**: Nivel DEBUG para scraping operations
- **Uso**: Desarrollo local rápido con datos de prueba
```bash
# Perfil por defecto
./mvnw spring-boot:run

# Explícitamente
./mvnw spring-boot:run -Dspring.profiles.active=dev
```

### 🐳 Perfil Docker (`docker`)  
- **Base de datos**: PostgreSQL (contenedor scraper-db)
- **Scraper API**: URL de servicio Docker (http://scraper-api:5000)
- **WebSocket**: Configuración de contenedor
- **Kafka**: Conexión a contenedor Kafka
- **URLs**: Nombres de servicios Docker para comunicación
- **Uso**: Entorno containerizado
```bash
export SPRING_PROFILES_ACTIVE=docker
docker-compose up --build
```

### 🏭 Perfil Producción (`prod`)
- **Base de datos**: PostgreSQL con optimizaciones de rendimiento
- **Scraper API**: URL de producción con balanceador
- **Pool de conexiones**: HikariCP optimizado para alta carga
- **CRON**: Intervalos optimizados para producción
- **WebSocket**: Configuración segura con SSL/TLS
- **Rate Limiting**: Control de velocidad de scraping
- **Uso**: Despliegue en producción
```bash
export SPRING_PROFILES_ACTIVE=prod
./mvnw spring-boot:run
```

## 📊 Variables de Entorno

Cada perfil usa variables diferentes desde el archivo `.env`:

```env
# Database Configuration
DB_HOST=localhost           # docker: scraper-db, prod: tu-host-prod
DB_PORT=5432
DB_NAME=pageAlert
DB_USERNAME=usuario
DB_PASSWORD=contraseña

# Scraper Configuration
SCRAPER_URL=http://localhost:5000/api/v1    # API externa de scraping
SCRAPER_API_KEY=default-api-key             # Clave para API scraper
SCRAPER_INTERVAL=300000                     # Intervalo en ms (5 min)

# WebSocket Configuration  
WEBSOCKET_ALLOWED_ORIGINS=http://localhost:5173  # Frontend URL
WEBSOCKET_MAX_CONNECTIONS=100

# Kafka Configuration
KAFKA_BOOTSTRAP_SERVERS=localhost:9092      # docker: kafka:9092
KAFKA_TOPIC_CHANGES=page-changes

# Logging
LOGGING_LEVEL=INFO          # dev: DEBUG, docker: INFO, prod: WARN
```

## 🛠️ Instalación y Ejecución

### Desarrollo Local (H2)
```bash
cd scraper-service
./mvnw spring-boot:run
# Accede a: http://localhost:4015
# Swagger: http://localhost:4015/swagger-ui/index.html
```

### Docker (PostgreSQL)
```bash
cd scraper-service
docker-compose up --build
# O desde la raíz: docker-compose up scraper-service
```

### Producción
```bash
export SPRING_PROFILES_ACTIVE=prod
# Configurar todas las variables de entorno de producción
./mvnw spring-boot:run
```

## 📊 Endpoints API

### GET /urls/users/{userId}
Lista todas las URLs monitorizadas por un usuario
```bash
curl http://localhost:4015/urls/users/123
```

### POST /urls/users/{userId}
Crea una nueva URL para monitorizar
```bash
curl -X POST http://localhost:4015/urls/users/123 \
  -H "Content-Type: application/json" \
  -d '{
    "url": "https://example.com",
    "selector": ".main-content",
    "frequency": 300000
  }'
```

### PUT /urls/{urlId}
Actualiza configuración de una URL monitorizadas
```bash
curl -X PUT http://localhost:4015/urls/456 \
  -H "Content-Type: application/json" \
  -d '{
    "url": "https://example.com/updated",
    "selector": ".updated-content",
    "frequency": 600000
  }'
```

### DELETE /urls/{urlId}
Elimina una URL del monitoreo
```bash
curl -X DELETE http://localhost:4015/urls/456
```

## 🌐 WebSockets

### Conexión WebSocket
```javascript
// Frontend JavaScript
const socket = new WebSocket('ws://localhost:4015/websocket');

socket.onmessage = function(event) {
  const change = JSON.parse(event.data);
  console.log('Change detected:', change);
};
```

### Eventos WebSocket
```json
{
  "type": "PAGE_CHANGE",
  "urlId": 123,
  "url": "https://example.com",
  "timestamp": "2024-01-15T10:30:00Z",
  "changes": {
    "selector": ".price",
    "oldValue": "$100",
    "newValue": "$80"
  }
}
```

## 🕷️ Configuración de Scraping

### UrlDTO - Estructura de datos
```json
{
  "id": 123,
  "url": "https://example.com/producto",
  "selector": ".precio, .stock",
  "frequency": 300000,
  "active": true,
  "lastCheck": "2024-01-15T10:00:00Z",
  "userId": "user123"
}
```

### Configuración CRON
```yaml
# application-dev.yml
scraper:
  schedule:
    check-changes: "0 */5 * * * *"  # Cada 5 minutos
  retry:
    max-attempts: 3
    delay: 5000
```

## 🧪 Testing

### Verificar conexión
```bash
curl http://localhost:4015/actuator/health
```

### Test WebSocket
```bash
# Instalar wscat: npm install -g wscat
wscat -c ws://localhost:4015/websocket
```

### Testing scraping
```bash
# Crear URL para monitorizar
curl -X POST http://localhost:4015/urls/users/123 \
  -H "Content-Type: application/json" \
  -d '{
    "url": "https://httpbin.org/get",
    "selector": "body",
    "frequency": 60000
  }'

# Verificar que se creó
curl http://localhost:4015/urls/users/123
```

## 🔧 Integración con Kafka

### Producción de eventos
```java
// Cuando se detecta un cambio
@Service
public class ScraperService {
    
    @EventListener
    public void onPageChange(PageChangeEvent event) {
        // Enviar evento a Kafka
        kafkaTemplate.send("page-changes", event);
        
        // Enviar via WebSocket
        websocketService.broadcast(event);
    }
}
```

### Eventos Kafka
```json
{
  "eventType": "PAGE_CHANGE_DETECTED",
  "urlId": 123,
  "userId": "user123", 
  "url": "https://example.com",
  "changes": {
    "selector": ".price",
    "previousContent": "Previous content",
    "newContent": "New content",
    "timestamp": "2024-01-15T10:30:00Z"
  }
}
```

## 🔒 Configuraciones de Seguridad

### Rate Limiting
```yaml
# application-prod.yml  
scraper:
  rate-limit:
    requests-per-minute: 10
    burst-capacity: 20
  user-agent: "PageAlert-Scraper/1.0"
```

### WebSocket CORS
```java
@Configuration
public class WebSocketConfig {
    
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/websocket")
                .setAllowedOrigins("https://tu-frontend.com")
                .withSockJS();
    }
}
```

## 📝 Logging y Monitoreo

### Configuración logging
```yaml
# application-dev.yml
logging:
  level:
    es.oscasais.pa.scraper: DEBUG
    org.springframework.web.socket: DEBUG
    org.apache.kafka: INFO
```

### Métricas importantes
- **URLs activas monitorizadas**
- **Frecuencia de cambios detectados**  
- **Tiempo promedio de scraping**
- **Errores de conexión a URLs**
- **Conexiones WebSocket activas**

## 🚨 Troubleshooting

### Error: "WebSocket connection refused"
- **Causa**: Puerto WebSocket bloqueado o mal configurado
- **Solución**: Verificar puerto 4015 y configuración CORS

### Error: "Scraper API not reachable"
- **Causa**: Servicio externo de scraping no disponible
- **Solución**: Verificar `SCRAPER_URL` y conectividad

### Error: "Too many URLs for user"
- **Causa**: Límite de URLs por usuario excedido
- **Solución**: Implementar paginación o aumentar límite

### Error: "Kafka connection failed"
- **Causa**: Servicio Kafka no disponible
- **Solución**: `docker-compose up kafka` o verificar configuración

### Error: "Invalid selector"
- **Causa**: Selector CSS malformado
- **Solución**: Validar sintaxis CSS selector antes de guardar