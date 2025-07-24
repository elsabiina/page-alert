#!/bin/bash

# =============================================
# PlantUML Installer Script (Enhanced)
# Now with proper sudo log file permissions
# =============================================

# Initialize logging with proper permissions
LOG_FILE="/tmp/plantuml_installer_$(date +%s).log"
sudo touch "$LOG_FILE"
sudo chmod 644 "$LOG_FILE"

log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1" | sudo tee -a "$LOG_FILE" >/dev/null
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1"  # Also show to user
}

# Start logging
log "=== PlantUML Installation Started ==="
log "User: $(whoami)"
log "------------------------------------"

# Validate JAR path argument
if [ -z "$1" ]; then
    log "ERROR: No JAR path specified"
    echo "Usage: sudo $0 /path/to/plantuml.jar" | sudo tee -a "$LOG_FILE"
    exit 1
fi

JAR_PATH="$1"
INSTALL_DIR="/opt/plantuml"
BIN_PATH="/usr/local/bin/plantuml"

# Dependency checks
log "Checking dependencies..."
declare -A DEPENDENCIES=(
    ["java"]="openjdk-17-jre"
    ["dot"]="graphviz"
)

for cmd in "${!DEPENDENCIES[@]}"; do
    if ! command -v "$cmd" &>/dev/null; then
        log "Installing ${DEPENDENCIES[$cmd]}..."
        sudo apt install -y "${DEPENDENCIES[$cmd]}" || {
            log "ERROR: Failed to install ${DEPENDENCIES[$cmd]}"
            exit 1
        }
    else
        log "$cmd already installed: $(which $cmd)"
    fi
done

# Handle installation directory
log "Processing installation directory: $INSTALL_DIR"
if [ -d "$INSTALL_DIR" ]; then
    log "WARNING: Directory already exists"
    read -p "Overwrite contents? [y/N] " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        sudo rm -rf "$INSTALL_DIR"/* || {
            log "ERROR: Failed to clean directory"
            exit 1
        }
    else
        log "Operation cancelled by user"
        exit 0
    fi
fi

sudo mkdir -p "$INSTALL_DIR" || {
    log "ERROR: Failed to create directory"
    exit 1
}

# Handle JAR file
log "Processing JAR file: $JAR_PATH"
if [ ! -f "$JAR_PATH" ]; then
    log "ERROR: JAR file not found at specified path"
    exit 1
fi

if [ -f "$INSTALL_DIR/plantuml.jar" ]; then
    log "WARNING: plantuml.jar already exists in target location"
    read -p "Overwrite? [y/N] " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        log "Operation cancelled by user"
        exit 0
    fi
fi

sudo mv "$JAR_PATH" "$INSTALL_DIR/plantuml.jar" || {
    log "ERROR: Failed to move JAR file"
    exit 1
}

# Create executable
log "Creating executable at $BIN_PATH"
if [ -f "$BIN_PATH" ]; then
    log "WARNING: Command wrapper already exists"
    read -p "Overwrite? [y/N] " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        log "Operation cancelled by user"
        exit 0
    fi
fi

sudo tee "$BIN_PATH" > /dev/null <<'EOF'
#!/bin/bash
java -jar /opt/plantuml/plantuml.jar "$@"
EOF

sudo chmod +x "$BIN_PATH" || {
    log "ERROR: Failed to make executable"
    exit 1
}

# Verification
log "Verifying installation..."
if which plantuml &>/dev/null; then
    VERSION=$(plantuml -version)
    log "SUCCESS: PlantUML installed - $VERSION"
    echo "Installation complete! Log saved to $LOG_FILE"
    echo "You can now use 'plantuml [OPTIONS]' globally."
else
    log "ERROR: Installation verification failed"
    exit 1
fi
