@echo off
echo 🚀 Starting Page Alert Microservices...
echo.

if "%1"=="down" goto stop
if "%1"=="build" goto build
if "%1"=="logs" goto logs
if "%1"=="restart" goto restart
if "%1"=="help" goto help
if "%1"=="-h" goto help
if "%1"=="--help" goto help

:start
echo 📦 Starting all services with Docker Compose...
docker-compose up -d
echo.
echo ✅ All services started!
echo 🌐 API Gateway: http://localhost:4010
echo 📚 Swagger UI: http://localhost:4010/swagger-ui/index.html
echo.
echo To see logs: docker-compose logs -f
echo To stop: docker-compose down
goto end

:stop
echo 🛑 Stopping all services...
docker-compose down
echo ✅ All services stopped!
goto end

:build
echo 🔨 Building all services...
docker-compose build
echo ✅ All services built!
goto end

:logs
echo 📋 Showing logs from all services...
docker-compose logs -f
goto end

:restart
echo 🔄 Restarting all services...
docker-compose down
docker-compose up -d
echo ✅ All services restarted!
goto end

:help
echo Usage: start-all.bat [OPTION]
echo.
echo Options:
echo   up          Start all services (default)
echo   down        Stop all services
echo   build       Build all services
echo   logs        Show logs from all services
echo   restart     Restart all services
echo   help        Show this help message
echo.
echo Examples:
echo   start-all.bat up
echo   start-all.bat down
echo   start-all.bat logs
goto end

:end
