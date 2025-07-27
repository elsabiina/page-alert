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

1. **Clona el repositorio**:  

   ```bash  
   ```  

2. **Levanta los servicios**:  

   ```bash  
   ```  

3. **Accede a los servicios**:  
   - API Docs: `http://localhost:<PORT>/swagger-ui/index.html`  
   - Frontend: `http://localhost:3000`  

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
