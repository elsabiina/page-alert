- En el uso de DOCKER son tremendamente inoportunas las variables de entorno. Typos: ideas para manejarlas?
<!-- TODO -->
- Disaster recovery

<http://localhost:4000/swagger-ui/index.htm>

## Prerequisitos

Se asume que el SO utilizado es Linux.

### Dependencias

> - [dbml-render](https://github.com/softwaretechnik-berlin/dbml-renderer) Generación de svg a partir de archivos dbml (Diagrama ER de base de datos).
>   - Necesita NodeJS
> - [PlantUML](https://github.com/plantuml/plantuml): Convertir plantUML a PNG
>   - Necesita JAVA
> - [Pandoc](https://pandoc.org/): Convertir docunentación a pdf

### Pasos a seguir

#### Instalar dependencias de Node globales

```bash
# Dependencias para diagaramas de Base de datos
npm install -g @dbml/cli @softwaretechnik/dbml-renderer

# Dependiencias para la generación de pdfs
sudo apt install -y pandoc texlive-full librsvg2-bin
```

#### Instalación de plantUML

> [Instrucciones online oficiales](https://plantuml.com/es/starting)

1. Visita [la página de descarga](https://plantuml.com/es/download) para descargar la última versión del `.jar`
2. Ejecutar el script de instalación que se encargará de instalar `graphviz` y `plantuml`

```bash
sudo ./install_plantuml.sh [PATH_TO_DOWNLOAD_PLANTUML_JAR]
```

#### instrucciones para la generación de documentación
