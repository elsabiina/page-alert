# Planificación

## Objetivos

La aplicación `page_alert` permite a usuarios conectarse a un sistema que se encargará de **monitorear** páginas web y de **avisarle** en caso de que alguna de las reglas (previamente definidas por el usuario) se cumpla.

## Funcionalidades

- Login de usuario.
- Lista de páginas y reglas de usuario.
- Ayuda 'interactiva' para la creación de reglas.
- CRUD de páginas y reglas.
- Monitoreo recurrente de las páginas.
- Notificación por la vía configurada de los cambios encontrados.

## Cronograma

| Fase                                                  | Fechas    | Hitos                                                                                                                           |
| ----------------------------------------------------- | --------- | ------------------------------------------------------------------------------------------------------------------------------- |
| Análisis y diseño                                     | 18-21 Jul | [&nbsp;] Diagramas UML (BD, JAVA) <br> [&nbsp;] Creación de script BD                                                           |
| Implementación [Server](./2_design_analysis#Server)   | 22-23 Jul | [&nbsp;] Proyecto base con todas las dependencias [0.1.0](#dependencias-server-0.1.0) <br> [&nbsp;] Conexión con Base de Datos  <br> [&nbsp;] CRUD (users, notifications) |
| Implementación [Scraper](./2_design_analysis#Scraper) | 24 Jul    | [&nbsp;] Proyecto base con todas las dependencias [0.1.0](#dependencias-scraper-0.1.0) <br> [&nbsp;] Conexión con Base de Datos <br> [&nbsp;] CRUD (urls, user_has_urls) |

## Detalle de versiones

### Requesitos generales `0.1.0`

- **Notificaciones**
  - Email
  - Push
- **Reglas de Monitoreo**:
  - Toda la página: `bodyHash`
  - Elemento/s concrectos: `['css-selector']`
  - Precio (automático)

### Dependencias Server `0.1.0`

- Spring Security `Security`
- PostgreSQL Driver `SQL`
- Spring Data JPA `SQL`
- H2 Database `SQL` (para desarrollo: base de datos en memoria)
- Spring Web `Web`
- Spring Boot DevTools Developer `Tools` (para desarrollo: reloads, restarts...)
- Validation `I/O`

### Dependencias Scraper `0.1.0`

- TODO

### Requesitos generales `0.2.0`

- **Notificaciones**
- **Reglas de Monitoreo**:
  - Añadir **ayuda interactiva** para la creación de reglas:
    - Elemento/s concrectos: `['css-selector']`
    - Precio
