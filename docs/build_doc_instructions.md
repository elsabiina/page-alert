```bash
# Inicia un servidor Web local con la documentadión 
docsify serve docs 
http://localhost:3000

```

**Creación de pfds**

```bash
# Proceso de build
# Correr en bash
./scripts/generate_diagram_images.sh
./scripts/monitor_version.sh

```

y luego (de momento solo he conseguido hacerlo correctamente en Windows)

```powershell
## creación de pdf
cd %userprofile%/page-alert/docs
./scripts/generate_pdf.ps1 .
```
