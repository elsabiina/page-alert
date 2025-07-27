param(
    [Parameter(Mandatory=$true)]
    [string]$Dir
)

$ErrorActionPreference = "Stop"

$inputDir  = Resolve-Path $Dir
$outputDir = "$inputDir\pdf"
$outputPdf = "$outputDir\documentation.pdf"
$metaFile  = "$inputDir\pdf_metadata.yaml"
$filterLua = "$inputDir\assets\strip-caption.lua"

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

# 4) Lanzar pandoc (sin YAML temporal)
& pandoc $files `
  --from markdown `
  --to pdf `
  --template=eisvogel `
  --pdf-engine=xelatex `
  --toc --toc-depth=3 --number-sections --listings `
  --metadata-file="$metaFile" `
  $filterArg `
  -o "$outputPdf"

if (-not (Test-Path $outputPdf)) {
    Write-Error "❌ No se pudo generar el PDF."
    exit 1
}
Write-Host "✅ PDF generado → $outputPdf" -ForegroundColor Green
