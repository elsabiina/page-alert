#!/bin/bash
# =============================================
# Auto-Sorted Markdown to PDF Converter
# Usage: ./generate_pdf.sh /path/to/markdown/files
# Output: /path/to/markdown/files/pdf/documentation.pdf
# =============================================

# Check if path argument is provided
if [ -z "$1" ]; then
    echo "ERROR: Please specify the directory containing Markdown files"
    echo "Usage: $0 /path/to/files"
    exit 1
fi

INPUT_DIR="$(realpath "$1")"  # Get absolute path
OUTPUT_DIR="${INPUT_DIR}/pdf"
OUTPUT_FILE="${OUTPUT_DIR}/documentation.pdf"
TEMPLATE="eisvogel"  # Remove if not using template

# Verify directory exists
if [ ! -d "$INPUT_DIR" ]; then
    echo "ERROR: Directory not found: $INPUT_DIR"
    exit 1
fi

# Check pandoc
if ! command -v pandoc &>/dev/null; then
    echo "ERROR: pandoc is required but not installed."
    echo "Install with:"
    echo "  Ubuntu: sudo apt install pandoc texlive-xetex texlive-latex-extra"
    echo "  Mac: brew install pandoc basictex"
    exit 1
fi

# Create pdf subdirectory if needed
mkdir -p "$OUTPUT_DIR"

# Get sorted files (1_xxx.md, 2_yyy.md, etc.) - use absolute paths
files=($(find "$INPUT_DIR" -maxdepth 1 -name "[0-9]_*.md" | sort -t_ -n -k1))

if [ ${#files[@]} -eq 0 ]; then
    echo "ERROR: No numbered Markdown files (1_xxx.md) found in $INPUT_DIR"
    exit 1
fi

echo "Processing files in order:"
printf "  • %s\n" "${files[@]##*/}"

# Generate PDF - Execute from INPUT_DIR to resolve relative paths
echo "Generating PDF..."
(
  cd "$INPUT_DIR"
  pandoc \
    --from markdown \
    --to pdf \
    --template="$TEMPLATE" \
    --toc \
    --toc-depth=2 \
    --number-sections \
    --pdf-engine=xelatex \
    -o "$OUTPUT_FILE" \
    "${files[@]##*/}"  # Use only filenames since we're in the directory
)

# Result
if [ -f "$OUTPUT_FILE" ]; then
    echo "SUCCESS: PDF generated at"
    echo "  $OUTPUT_FILE"
else
    echo "ERROR: Failed to generate PDF"
    exit 1
fi
