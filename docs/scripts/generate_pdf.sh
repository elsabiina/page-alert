#!/usr/bin/env bash
set -euo pipefail

[ -z "$1" ] && {
  echo "Uso: $0 /ruta/a/markdown"
  exit 1
}

INPUT_DIR="$(realpath "$1")"
OUTPUT_DIR="$INPUT_DIR/pdf"
OUTPUT_FILE="$OUTPUT_DIR/documentation.pdf"
METADATA_FILE="$INPUT_DIR/pdf_metadata.yaml"
FILTER_FILE="$INPUT_DIR/assets/strip-caption.lua"

mkdir -p "$OUTPUT_DIR"

# Lista ordenada
files=()
[ -f "$INPUT_DIR/_coverpage.md" ] && files+=("$INPUT_DIR/_coverpage.md")
[ -f "$INPUT_DIR/README.md" ] && files+=("$INPUT_DIR/README.md")
mapfile -t numbered < <(find "$INPUT_DIR" -maxdepth 1 -type f -name '[0-9]_*.md' | sort -t_ -n -k1)
files+=("${numbered[@]}")
[ ${#files[@]} -eq 0 ] && {
  echo "No hay Markdown"
  exit 1
}

echo "Orden:"
printf " • %s\n" "${files[@]##*/}"

# Metadatos
TITLE=${TITLE:-"Documentación del producto"}
AUTHOR=${AUTHOR:-"Equipo de desarrollo"}
VERSION=${VERSION:-"v1.0.0"}
DATE=$(date +"%d/%m/%Y")

# Preparar YAML temporal con colores
cat >"$OUTPUT_DIR/_temp.yaml" <<EOF
---
title: "$TITLE"
author: "$AUTHOR"
date: "$DATE"
version: "$VERSION"
titlepage: true
titlepage-color: "3C4043"
titlepage-text-color: "FFFFFF"
titlepage-rule-color: "4285F4"
titlepage-rule-height: 2
...
EOF

# Filtro solo si existe
FILTER_ARG=()
[ -f "$FILTER_FILE" ] && FILTER_ARG=(--lua-filter="$FILTER_FILE")

# Generar
pandoc "${files[@]}" \
  --metadata-file="$OUTPUT_DIR/_temp.yaml" \
  --from markdown --to pdf \
  --template=eisvogel \
  --pdf-engine=xelatex \
  --toc --toc-depth=3 --number-sections --listings \
  "${FILTER_ARG[@]}" \
  -o "$OUTPUT_FILE"

rm -f "$OUTPUT_DIR/_temp.yaml"
echo "✅ PDF → $OUTPUT_FILE"
