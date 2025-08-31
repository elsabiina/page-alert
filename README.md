# 🚀 **Microservicios de Cacharreo Técnico**  

**¡Bienvenido a mi proyecto de "cacharreo" tecnológico!** 🔧💻  
Un *sandbox* donde exploro las tecnologías que veo por el Internet, intenando implementar microservicios lo más independientes posible.  
**Mi Objetivo** es aprender, romper (-me la cabeza) y arreglarla (o no 😅).  

En estos años como desarrollador centrado en el ***Front End***  el síndrome del impostor no me ha abandonado. Es por esto que empecé un curso de OOP con Java y este proyecto es el resultado final.

## 🛠 **Tecnologías Usadas**  

### 🤖 **Backend**  

- <img src="https://img.icons8.com/color/48/spring-logo.png" style="vertical-align: middle" alt="spring-logo" width="" height="20"> **Spring Boot**: El corazón del proyecto. Mis primeros pasos como BE.
- 🔒 **Spring Security + OAuth2/OpenID**: Muy interesante la gestión de sesiones con el Tomcat embedido pero el FE debía de ir fuera.  
- 🗃️ **JPA + PostgreSQL**: Persistencia casi mágica, aunque algún dolor de sincronía (a ver si me da tiempo a investigar Liquibase vs Flyway)
- ⚡ **gRPC**: Comunicación *síncrona* entre microservicios (todo un reto y una gran novedad para mí, no había escuchado hablar).  
- 📨 **Kafka**: Mensajería *asíncrona* hoy procesos que requieren su tiempo.  
- 🌐 **WebSocket**: Conexión en tiempo real con el frontend (bye, HTTP polling).  

### 🎨 **Frontend**  

- <img src="https://vuejs.org/images/logo.png" width="16" height="16"> **Vue.js**: Alguno tenía que escoger.  

### <img src="https://img.icons8.com/external-those-icons-lineal-color-those-icons/48/external-Docker-social-media-those-icons-lineal-color-those-icons.png" style="vertical-align: middle" alt="docker-logo" witdth="40px" height="40px"></img> **Infra & DevOps**  

- **Docker**: Ampliamente adpotado en la industria y con mucha documentación e integración.
- **Docker Compose**: Orquestación local, Kubernetes en producción (pensándomelo).  

### 📜 **Documentación**  

- <img src="https://img.icons8.com/?size=18&id=rdKV2dee9wxd&format=png" style="vertical-align: middle" width="18" height="18"> **Swagger/OpenAPI**: Documentación automática (la visibilidad es muy importante).  
- Documentación a más alto nivel y más detallada disponible en [`docs`](./docs/README.md#documentación-page_alert)

---

## 🏗️ **Estructura del Proyecto**  

He decido meterlo todo en un solo repo para facilitarme la vida. Cada Servicio es un proyecto de JAVA completamente independiente así que se pueden descargar de manera individual.

```  
├── /api-guard            🔐 Autenticación y seguridad centralizada, Comunicación transparente para el FE  
├── /auth-service         🔐 Autenticación (OAuth2 + JWT)  
├── /user-service         👥 Gestión de usuarios (JPA + PostgreSQL)  
├── /notification-service 👥 Avisa (email, push notification) a los usuarios de los cambios detectados en URLs que sigue   
├── /scraper-service      💬 CRON para hacer webScrapper + WebSockets (asistena al usuario para elección de reglas de Scrapping).  
├── /frontend             🖥️ Integración con auth-service  
├── /docs                 📚 Swagger,swagger arquitectura, diagrams, scripts de generación la documentación misma, etc.  
```  

## 🚦 **Cómo Arrancar el Proyecto**  

### 🚀 **Opción 1: Sistema Completo con Scripts (Work in progres)**

1. **Clona el repositorio**:  

   ```bash  
   git clone <repository-url>
   cd page-alert
   ```  

2. **Configura las variables de entorno** para TODOS los servicios. Por ejemplo en `notification-service/.env`:

   ```bash
   # Edita notification-service/.env
   SPRING_MAIL_USERNAME=tu-email@gmail.com
   SPRING_MAIL_PASSWORD=tu-app-password
   ```

3. **Levanta todos los servicios** usando los scripts de ayuda:  

   ```bash  
   # Modo Desarrollo (con Swagger habilitado)
   ./start-all.sh up        # Linux/Mac
   start-all.bat up         # Windows
   
   # Modo Producción (Swagger deshabilitado por seguridad)
   ./start-all.sh prod      # Linux/Mac
   start-all.bat prod       # Windows
   
   # O directamente con Docker Compose
   docker-compose up --build  # Desarrollo
   ```  

4. **Gestiona los servicios**:

   ```bash
   ./start-all.sh up       # Desarrollo con Swagger habilitado
   ./start-all.sh prod     # Producción con Swagger deshabilitado
   ./start-all.sh down     # Parar todos los servicios  
   ./start-all.sh logs     # Ver logs en tiempo real
   ./start-all.sh restart  # Reiniciar todos los servicios
   ./start-all.sh build    # Construir todas las imágenes
   ./start-all.sh help     # Mostrar ayuda
   ```

5. **Accede al sistema**:  
   - **API Gateway**: `http://localhost:4010` (único punto de entrada)
   - **Frontend**: `http://localhost:3000` (cuando esté disponible)

### 🔧 **Opción 2: Desarrollo de Servicios Individuales**

Cada servicio mantiene su propio `.env` y `docker-compose.yml` para desarrollo independiente. Asegurate de tener todas ls variables de entorno en el `.env` y luego corre por ejeamplo para el `auth-service`:

```bash
cd auth-service
docker-compose up --build
# Usa su propia BD PostgreSQL y configuración
```

### ⚙️ **Nuevos Perfiles de Spring Boot**

Cada microservicio ahora soporta 3 perfiles diferentes:

#### **🛠️ Perfil de Desarrollo (`dev`)**
- **Base de datos**: H2 en memoria (sin instalación)
- **URLs**: localhost para comunicación entre servicios
- **Uso**: Desarrollo local rápido
```bash
cd auth-service
./mvnw spring-boot:run  # Usa 'dev' por defecto
# O explícitamente:
./mvnw spring-boot:run -Dspring.profiles.active=dev
```

#### **🐳 Perfil Docker (`docker`)**  
- **Base de datos**: PostgreSQL (contenedores)
- **URLs**: Nombres de servicios de Docker para networking
- **Uso**: Entorno containerizado
```bash
cd auth-service
export SPRING_PROFILES_ACTIVE=docker
docker-compose up --build
```

#### **🏭 Perfil Producción (`prod`)**
- **Base de datos**: PostgreSQL optimizado 
- **Configuración**: Pool de conexiones, logging mínimo, seguridad reforzada
- **Uso**: Despliegue en producción
```bash
export SPRING_PROFILES_ACTIVE=prod
./mvnw spring-boot:run
```

**Configuración de perfiles:**
- Las variables de entorno siguen funcionando desde los archivos `.env`
- Cada perfil tiene su archivo `application-{profile}.yml` específico
- El archivo `application.yml` principal define el perfil por defecto como `dev`

### 📚 **Documentación API**

⚠️ **Importante**: Los endpoints de Swagger están **deshabilitados en producción** por seguridad.

#### **🔧 Modo Desarrollo (`./start-all.sh up`)**

| Servicio | Desarrollo Individual |
|------------------------|----------------------|
| **Hub de Documentación** | - |
| **Auth Service** | `http://localhost:4005/swagger-ui/index.html` |
| **User Service** | `http://localhost:4000/swagger-ui/index.html` |
| **Notification Service** | `http://localhost:4020/swagger-ui/index.html` |
| **Scraper Service** | `http://localhost:4015/swagger-ui/index.html` |

### 📊 **Puertos y Acceso**

| Servicio | Puerto Interno | Puerto Externo | Desarrollo Individual | Acceso |
|----------|----------------|----------------|----------------------|---------|
| API Gateway | 4010 | 4010 | - | ✅ Público |
| Auth Service | 4005 | - | 4005 | 🔒 Solo interno |
| User Service | 4000 | - | 4000 | 🔒 Solo interno |
| Notification Service | 4020 | - | 4020 | 🔒 Solo interno |
| Scraper Service | 4015 | - | 4015 | 🔒 Solo interno |

### 📁 **Estructura de Configuración**

```
page-alert/
├── docker-compose.yml          # Configuración base (Kafka + API Gateway)
├── docker-compose.override.yml # Orquestación de servicios individuales
├── docker-compose.prod.yml     # Configuración específica de producción
├── start-all.sh               # Script de ayuda Linux/Mac
├── start-all.bat              # Script de ayuda Windows
├── auth-service/
│   ├── .env                   # Variables específicas del servicio
│   └── docker-compose.yml     # Configuración independiente
├── user-service/
│   ├── .env                   # Variables específicas del servicio  
│   └── docker-compose.yml     # Configuración independiente
├── notification-service/
│   ├── .env                   # Variables específicas del servicio
│   └── docker-compose.yml     # Desarrollo + integración
└── api-guard/
    └── src/main/resources/
        └── application.yml     # Perfiles development/production
```

## 🤔 **¿Por Qué Este Proyecto?**  

- **Aprender**: Integrar tecnologías modernas en un entorno real(ista).  
- <span style="
    font-family: 'Comic Sans MS', cursive;
    color: #00FF00;
    background-color: #FF00FF;
    font-size: 1em;
    letter-spacing: 2px;
    word-spacing: -5px;
    line-height: 0.8;
    text-shadow: 3px 3px 0px #FFFF00, 3px -3px 0px #00FFFF;
    transform: rotate(2deg) skewX(15deg);
    animation: flash 0.3s infinite;
    display: inline-block;
    padding: 5px 15px;
    border: 3px dashed #FF0000;
    border-radius: 50% 20% 70% 30%;
">Incomodarme</span>: Llevaba mucho tiempo queriendo cacharrear con el Backend pero nunca tenía tiempo ni sabía qué podía hacer.  
- **Divertirme**: Programar me divertía hasta que empecé a estar estancado en un Rol durante mucho tiempo. Me ha vuelto a diverir!  

## 📜 **Licencia**  

MIT License (¡¡viva el🩷👭A🩷🌈M👭🌈O👬🌈R💏🩷🌈  Y el conocimiento libre!!).  

> *"Cacharrear es el camino y se cacharrea al programar"* — *Alguien en Internet* 😉
