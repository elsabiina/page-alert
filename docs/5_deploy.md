# Despliegue

El despliegue lo haremos utilizando `docker-compose`.
Cada uno de los Componentes a desplegar tendrá un archivo `Docker` que se encargará de levantar el servicio por nosotros.

Esto nos permite fácilmente desplegarlo tanto en entornos de desarrollo como a producción.

En la implementación actual cada uno de los Componentes tendrá un archivo `.env` que es de donde cargará los ***secrets***. A futuro implementaremos esta lógica mediante archivos secrets que Docker no persiste en disco sino en memoria.

```yaml
# Serve only as an example
# docker-compose.yml
secrets:
   db_password:
     file: db_password.txt
   db_root_password:
     file: db_root_password.txt
```
