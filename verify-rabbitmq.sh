#!/bin/bash

# Script de verificación de integración RabbitMQ
# Club Los Amigos - Sistema de Gestión de Turnos

echo "========================================="
echo "  VERIFICACIÓN INTEGRACIÓN RABBITMQ"
echo "========================================="
echo ""

# Verificar archivos Java de RabbitMQ
echo "📁 Verificando archivos de RabbitMQ..."
RABBITMQ_FILES=(
    "Backend/src/main/java/com/clublosamigos/turnero/config/RabbitMQConfig.java"
    "Backend/src/main/java/com/clublosamigos/turnero/dto/message/NotificationMessage.java"
    "Backend/src/main/java/com/clublosamigos/turnero/dto/message/BulkGenerationMessage.java"
    "Backend/src/main/java/com/clublosamigos/turnero/service/MessageProducerService.java"
    "Backend/src/main/java/com/clublosamigos/turnero/service/MessageConsumerService.java"
    "Backend/src/main/java/com/clublosamigos/turnero/config/RestTemplateConfig.java"
)

for file in "${RABBITMQ_FILES[@]}"; do
    if [ -f "$file" ]; then
        echo "   ✅ $file"
    else
        echo "   ❌ FALTA: $file"
    fi
done

echo ""
echo "📦 Verificando dependencias en pom.xml..."
if grep -q "spring-boot-starter-amqp" Backend/pom.xml; then
    echo "   ✅ spring-boot-starter-amqp encontrada"
else
    echo "   ❌ FALTA: spring-boot-starter-amqp"
fi

echo ""
echo "⚙️  Verificando configuración..."
if grep -q "rabbitmq:" Backend/src/main/resources/application.yml; then
    echo "   ✅ Configuración RabbitMQ en application.yml"
else
    echo "   ❌ FALTA configuración RabbitMQ"
fi

echo ""
echo "🐳 Verificando Docker Compose..."
if grep -q "rabbitmq:" Arquitectura/docker-compose.yml; then
    echo "   ✅ Servicio RabbitMQ en docker-compose.yml"
else
    echo "   ❌ FALTA servicio RabbitMQ"
fi

echo ""
echo "🗄️  Verificando scripts SQL..."
if [ -f "Arquitectura/init-scripts/03-notification-logs-migration.sql" ]; then
    echo "   ✅ Migration script para notification_logs"
else
    echo "   ❌ FALTA migration script"
fi

echo ""
echo "🔨 Verificando build del backend..."
if [ -f "Backend/target/turnero-backend.jar" ]; then
    JAR_SIZE=$(du -h Backend/target/turnero-backend.jar | cut -f1)
    echo "   ✅ JAR generado: $JAR_SIZE"

    # Verificar clases RabbitMQ en JAR
    if jar -tf Backend/target/turnero-backend.jar | grep -q "RabbitMQConfig"; then
        echo "   ✅ Clases RabbitMQ incluidas en JAR"
    else
        echo "   ❌ Clases RabbitMQ NO incluidas en JAR"
    fi
else
    echo "   ⚠️  JAR no generado. Ejecutar: cd Backend && mvn clean package -DskipTests"
fi

echo ""
echo "========================================="
echo "  RESUMEN"
echo "========================================="
echo ""
echo "Total archivos Java: $(find Backend/src/main/java -name '*.java' | wc -l)"
echo "Archivos RabbitMQ: ${#RABBITMQ_FILES[@]}"
echo ""
echo "Para iniciar el sistema:"
echo "  cd Arquitectura"
echo "  docker-compose up -d"
echo ""
echo "Acceder a:"
echo "  - Frontend: http://localhost:1999"
echo "  - RabbitMQ UI: http://localhost:15672 (guest/guest)"
echo "  - n8n: http://localhost:5678"
echo ""
echo "========================================="
