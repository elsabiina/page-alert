param(
    [Parameter(Mandatory=$true)]
    [string]$Dir
)

$ErrorActionPreference = "Stop"
$inputDir  = Resolve-Path $Dir
$outputDir = "$inputDir\pdf"
$outputPdf = "$outputDir\documentation.pdf"
$metaFile  = "$inputDir\pdf_metadata.yaml"
$filterLua = "$inputDir\scripts\remove-image-caption.lua"

# 1) Crear carpeta salida
New-Item -ItemType Directory -Force $outputDir | Out-Null

# 2) Armar lista ordenada
$files = @()
if (Test-Path "$inputDir\README.md")     { $files += "$inputDir\README.md" }
$files += Get-ChildItem "$inputDir\[0-9]_*.md" | Sort-Object Name

if ($files.Count -eq 0) { Write-Error "No hay Markdown"; exit 1 }

Write-Host "Orden:" -ForegroundColor Cyan
$files | ForEach-Object { Write-Host " • $($_.Name)" }

# 3) Filtro solo si existe
$filterArg = @()
if (Test-Path $filterLua) { $filterArg += "--lua-filter=`"$filterLua`"" }
# --include-in-header="scripts/header-includes.tex" `

# 4) Lanzar pandoc (sin YAML temporal)
& pandoc $files `
  --from markdown `
  --to pdf `
  --template=eisvogel `
  --pdf-engine=lualatex `
  --standalone `
  --toc --toc-depth=3 --number-sections --listings `
  --metadata-file="$metaFile" `
  -V linkcolor:pagealertgreen `
  -V urlcolor:pagealertgreen `
  -V citecolor:pagealertgreen `
  -V filecolor:magenta `
  -V hidelinks:false `
  -V mainfont="Source Sans Pro" `
  -V monofont="Source Code Pro" `
  -V sansfont="Source Sans Pro" `
  $filterArg `
  -o "$outputPdf"

if (-not (Test-Path $outputPdf)) {
    Write-Error "❌ No se pudo generar el PDF."
    exit 1
}
Write-Host "✅ PDF generado → $outputPdf" -ForegroundColor Green
