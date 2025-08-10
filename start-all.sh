#!/bin/bash

echo "🚀 Starting Page Alert Microservices..."
echo ""

# Función para mostrar ayuda
show_help() {
    echo "Usage: ./start-all.sh [OPTION]"
    echo ""
    echo "Options:"
    echo "  up          Start all services in development mode (default)"
    echo "  prod        Start all services in production mode (Swagger disabled)"
    echo "  down        Stop all services"
    echo "  build       Build all services"
    echo "  logs        Show logs from all services"
    echo "  restart     Restart all services"
    echo "  help        Show this help message"
    echo ""
    echo "Examples:"
    echo "  ./start-all.sh up      # Development mode with Swagger"
    echo "  ./start-all.sh prod    # Production mode without Swagger"
    echo "  ./start-all.sh down"
    echo "  ./start-all.sh logs"
}

# Función para verificar si Docker está corriendo
check_docker() {
    if ! docker info > /dev/null 2>&1; then
        echo "❌ Docker is not running. Please start Docker first."
        exit 1
    fi
}

# Función principal
case "${1:-up}" in
    "up")
        check_docker
        echo "📦 Starting all services in DEVELOPMENT mode..."
        export SPRING_PROFILES_ACTIVE=development
        docker-compose up -d
        echo ""
        echo "✅ All services started in DEVELOPMENT mode!"
        echo "🌐 API Gateway: http://localhost:4010"
        echo "📚 API Documentation Hub: http://localhost:4010/api/swagger/"
        echo "🔧 Swagger Endpoints:"
        echo "   • Auth Service: http://localhost:4010/api/swagger/auth/"
        echo "   • User Service: http://localhost:4010/api/swagger/users/"
        echo "   • Notification Service: http://localhost:4010/api/swagger/notifications/"
        echo "   • Scraper Service: http://localhost:4010/api/swagger/scraper/"
        echo ""
        echo "To see logs: docker-compose logs -f"
        echo "To stop: docker-compose down"
        ;;
    "prod")
        check_docker
        echo "📦 Starting all services in PRODUCTION mode..."
        export SPRING_PROFILES_ACTIVE=production
        docker-compose -f docker-compose.yml -f docker-compose.override.yml -f docker-compose.prod.yml up -d
        echo ""
        echo "✅ All services started in PRODUCTION mode!"
        echo "🌐 API Gateway: http://localhost:4010"
        echo "🔒 Swagger endpoints are DISABLED for security"
        echo ""
        echo "To see logs: docker-compose logs -f"
        echo "To stop: docker-compose down"
        ;;
    "down")
        echo "🛑 Stopping all services..."
        docker-compose down
        echo "✅ All services stopped!"
        ;;
    "build")
        check_docker
        echo "🔨 Building all services..."
        docker-compose build
        echo "✅ All services built!"
        ;;
    "logs")
        echo "📋 Showing logs from all services..."
        docker-compose logs -f
        ;;
    "restart")
        echo "🔄 Restarting all services..."
        docker-compose down
        docker-compose up -d
        echo "✅ All services restarted!"
        ;;
    "help"|"-h"|"--help")
        show_help
        ;;
    *)
        echo "❌ Unknown option: $1"
        echo ""
        show_help
        exit 1
        ;;
esac
