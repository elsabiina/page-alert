param(
    [Parameter(Position=0)]
    [ValidateSet("up", "prod", "down", "build", "logs", "restart", "help")]
    [string]$Action = "up"
)

# Función para mostrar ayuda
function Show-Help {
    Write-Host ""
    Write-Host "Usage: .\start-all.ps1 [OPTION]" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "Options:" -ForegroundColor Yellow
    Write-Host "  up          Start all services in development mode (default)" -ForegroundColor White
    Write-Host "  prod        Start all services in production mode (Swagger disabled)" -ForegroundColor White
    Write-Host "  down        Stop all services" -ForegroundColor White
    Write-Host "  build       Build all services" -ForegroundColor White
    Write-Host "  logs        Show logs from all services" -ForegroundColor White
    Write-Host "  restart     Restart all services" -ForegroundColor White
    Write-Host "  help        Show this help message" -ForegroundColor White
    Write-Host ""
    Write-Host "Examples:" -ForegroundColor Yellow
    Write-Host "  .\start-all.ps1 up      # Development mode with Swagger" -ForegroundColor Gray
    Write-Host "  .\start-all.ps1 prod    # Production mode without Swagger" -ForegroundColor Gray
    Write-Host "  .\start-all.ps1 down" -ForegroundColor Gray
    Write-Host "  .\start-all.ps1 logs" -ForegroundColor Gray
    Write-Host ""
}

# Función para verificar si Docker está corriendo
function Test-Docker {
    try {
        $null = docker info 2>$null
        return $true
    }
    catch {
        Write-Host "Error: Docker is not running. Please start Docker first." -ForegroundColor Red
        exit 1
    }
}

# Función para mostrar información de servicios
function Show-ServiceInfo {
    param([bool]$IsProduction = $false)
    
    Write-Host ""
    if ($IsProduction) {
        Write-Host "All services started in PRODUCTION mode!" -ForegroundColor Green
    } else {
        Write-Host "All services started in DEVELOPMENT mode!" -ForegroundColor Green
    }
    
    Write-Host "API Gateway: http://localhost:4010" -ForegroundColor Cyan
    
    if (-not $IsProduction) {
        Write-Host "API Documentation Hub: http://localhost:4010/api/swagger/" -ForegroundColor Cyan
        Write-Host "Swagger Endpoints:" -ForegroundColor Yellow
        Write-Host "   Auth Service: http://localhost:4010/api/swagger/auth/" -ForegroundColor Gray
        Write-Host "   User Service: http://localhost:4010/api/swagger/users/" -ForegroundColor Gray
        Write-Host "   Notification Service: http://localhost:4010/api/swagger/notifications/" -ForegroundColor Gray
        Write-Host "   Scraper Service: http://localhost:4010/api/swagger/scraper/" -ForegroundColor Gray
    } else {
        Write-Host "Swagger endpoints are DISABLED for security" -ForegroundColor Yellow
    }
    
    Write-Host ""
    Write-Host "To see logs: docker-compose logs -f" -ForegroundColor Gray
    Write-Host "To stop: docker-compose down" -ForegroundColor Gray
}

# Banner inicial
Write-Host "Starting Page Alert Microservices..." -ForegroundColor Magenta
Write-Host ""

# Procesar acciones
switch ($Action.ToLower()) {
    "up" {
        Test-Docker
        Write-Host "Starting all services in DEVELOPMENT mode..." -ForegroundColor Yellow
        $env:SPRING_PROFILES_ACTIVE = "development"
        
        try {
            docker-compose up -d
            if ($LASTEXITCODE -eq 0) {
                Show-ServiceInfo -IsProduction $false
            } else {
                Write-Host "Failed to start services" -ForegroundColor Red
                exit 1
            }
        }
        catch {
            Write-Host "Error starting services: $_" -ForegroundColor Red
            exit 1
        }
    }
    
    "prod" {
        Test-Docker
        Write-Host "Starting all services in PRODUCTION mode..." -ForegroundColor Yellow
        $env:SPRING_PROFILES_ACTIVE = "production"
        
        try {
            docker-compose -f docker-compose.yml -f docker-compose.override.yml -f docker-compose.prod.yml up -d
            if ($LASTEXITCODE -eq 0) {
                Show-ServiceInfo -IsProduction $true
            } else {
                Write-Host "Failed to start services in production mode" -ForegroundColor Red
                exit 1
            }
        }
        catch {
            Write-Host "Error starting services in production mode: $_" -ForegroundColor Red
            exit 1
        }
    }
    
    "down" {
        Write-Host "Stopping all services..." -ForegroundColor Yellow
        try {
            docker-compose down
            if ($LASTEXITCODE -eq 0) {
                Write-Host "All services stopped!" -ForegroundColor Green
            } else {
                Write-Host "Failed to stop services" -ForegroundColor Red
                exit 1
            }
        }
        catch {
            Write-Host "Error stopping services: $_" -ForegroundColor Red
            exit 1
        }
    }
    
    "build" {
        Test-Docker
        Write-Host "Building all services..." -ForegroundColor Yellow
        try {
            docker-compose build
            if ($LASTEXITCODE -eq 0) {
                Write-Host "All services built!" -ForegroundColor Green
            } else {
                Write-Host "Failed to build services" -ForegroundColor Red
                exit 1
            }
        }
        catch {
            Write-Host "Error building services: $_" -ForegroundColor Red
            exit 1
        }
    }
    
    "logs" {
        Write-Host "Showing logs from all services..." -ForegroundColor Yellow
        Write-Host "Press Ctrl+C to exit logs" -ForegroundColor Gray
        Write-Host ""
        try {
            docker-compose logs -f
        }
        catch {
            Write-Host "Error showing logs: $_" -ForegroundColor Red
            exit 1
        }
    }
    
    "restart" {
        Write-Host "Restarting all services..." -ForegroundColor Yellow
        try {
            docker-compose down
            if ($LASTEXITCODE -eq 0) {
                docker-compose up -d
                if ($LASTEXITCODE -eq 0) {
                    Write-Host "All services restarted!" -ForegroundColor Green
                } else {
                    Write-Host "Failed to restart services" -ForegroundColor Red
                    exit 1
                }
            } else {
                Write-Host "Failed to stop services for restart" -ForegroundColor Red
                exit 1
            }
        }
        catch {
            Write-Host "Error restarting services: $_" -ForegroundColor Red
            exit 1
        }
    }
    
    "help" {
        Show-Help
    }
    
    default {
        Write-Host "Unknown option: $Action" -ForegroundColor Red
        Show-Help
        exit 1
    }
}
