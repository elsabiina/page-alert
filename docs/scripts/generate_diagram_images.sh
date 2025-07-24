#!/bin/bash

# Configuración de rutas
SCRIPT_DIR=$(dirname "$(readlink -f "$0")")
DOCS_DIR=$(dirname "$SCRIPT_DIR")
DIAGRAMS_DIR="$DOCS_DIR/diagrams"
ASSETS_DIR="$DOCS_DIR/assets"

if [ ! -d "$DIAGRAMS_DIR" ]; then
    echo "Error: No se encontró la carpeta diagrams en $DIAGRAMS_DIR"
    exit 1
fi

mkdir -p "$ASSETS_DIR"

echo "Generando diagramas de PLantUML desde: $DIAGRAMS_DIR"
echo "Guardando imágenes en: $ASSETS_DIR"

find "$DIAGRAMS_DIR" -type f -name "*.puml" -print0 | while IFS= read -r -d '' file; do
    echo "Procesando: $(basename "$file")"
    plantuml -o "$ASSETS_DIR" "$file" || echo "Error al procesar: $file"
done

if [[ ! -f "$SCRIPT_DIR/monitor_version.sh" ]]; then
    echo "Error: Script not found at $SCRIPT_DIR/monitor_version.sh"
    exit 1
fi

if [[ ! -d "$ASSETS_DIR" ]]; then
    echo "Error: Assets directory not found at $ASSETS_DIR"
    exit 1
fi


if ! command -v dbml-renderer &> /dev/null; then
    echo "Error: dbml-renderer is not installed"
    echo "Install it with: npm install -g @softwaretechnik/dbml-renderer"
    exit 1
fi


echo "Generando diagram de Base de Datos desde: $DIAGRAMS_DIR"
echo "Guardando imágenes en: $ASSETS_DIR"

find "$DIAGRAMS_DIR" -type f -name "*.dbml" | while read -r dbml_file; do
    # Obtener nombre sin extensión ni path
    filename=$(basename -- "$dbml_file")
    filename_noext="${filename%.*}"
    dirname=$(dirname -- "$dbml_file")
    
    # Generar nombre de archivo SVG
    svg_file="${ASSETS_DIR}/${filename_noext}.svg"
    
    echo "Convirtiendo: $filename a SVG..."
    
    # Ejecutar conversión
    if dbml-renderer -i "$dbml_file" -o "$svg_file"; then
        echo "✅ Éxito: $svg_file creado"
    else
        echo "❌ Error al convertir $filename"
    fi
done

"$SCRIPT_DIR/monitor_version.sh" "$ASSETS_DIR"

echo "Proceso completado"
