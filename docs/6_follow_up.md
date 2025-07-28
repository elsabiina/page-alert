# Seguimiento

**Progreso realizado**  

- Configuración inicial del entorno Docker con PostgreSQL y Spring Boot.  
- Creación del contenedor `user-service-db` y mapeo de puerto 5000→5432.  
- Creación del microservicio `user-service` y mapeo de puerto 4000→4000.  
- Integración exitosa de la aplicación con la base de datos. También memoria para los futuros test unitarios.  
- Desarrollo del script `data.sql` con 20 registros de prueba.  

**Desafíos encontrados**  

- **Fallos en Configuración inicial**:
  - El IDE no me reconocía los módulos dentro del proyecto padre.  
    **Solución**: ponerlos como dependencia en el `pom.xml` del padre.
  - Evitar nombre `user` como tabla (da error con H2 database (la que se levanta en memoria), en el script de generación evitar uso de doble comilla "" para nombre de atributos y de tablas.
  - **Conflicto de Schema entre H2 y PostgreSQL**:  Que hacía que el servicio no se levantara. El problema estaba en cómo definíamos la PK en el `data.sql`.
    **Solución**: Hacer el script inicial mucho más sencillo (es solo un script para testing)  
  - **Error de inicialización de Docker**: Fallo al iniciar `user-service` por conexión prematura a `user-service-db`.  
  *Solución*: añadir `healthcheck` en PostgreSQL y `depends_on condition: service_healthy`.  

### Plan de acción para la finalización  

Es todavía pronto, pero diría que va a ser harto difícil acabar el proyecto tal cual lo hemos planteado con fecha **31/07/2025**.  

A día de hoy tan solo hemos acabado 1/5 componentes de BackEnd y falta el FrontEnd. Debemos intentar dar un enfoque más práctico.

**Acciones que vamos a tomar**

- Vamos a pasar de puntillas por el tema de la seguridad: Al final, el día de la entrega vamos a estar corriendo todo en local y es con el objetivo de ofrecer una prueba de concepto.
- No vamos a implementar la comunicación RPC entre `user-service` y `scraper-service`
- El `notification-service` podríamos abandonarlo y ver la posibilidad de usar una herramienta externa como [apprise](https://github.com/caronc/apprise)

  ```bash
  docker run -d -p 8000:8000 --name apprise caronc/apprise-api
  ```
  
  ```java
  record ApprisePayload(String urls, String title, String body) {}
  
  RestTemplate rest = new RestTemplate();
  ApprisePayload payload = new ApprisePayload(
          "discord://webhook_id/webhook_token",
          "Alerta",
          "¡Todo está arriba!"
  );
  rest.postForLocation("http://localhost:8000/notify", payload);
  ```

La prioridad es tener un sistema funcional que sirva a modo de Demostración.
