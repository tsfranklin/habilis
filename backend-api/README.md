# Backend API - Habilis

## 📋 Descripción
Backend API REST desarrollado con Spring Boot 3.2.0 y Java 17 para el proyecto Habilis.

## 🚀 Tecnologías
- **Java**: 17
- **Spring Boot**: 3.2.0
- **Base de Datos**: PostgreSQL 15
- **ORM**: JPA/Hibernate
- **Build Tool**: Maven

## 📦 Dependencias Principales
- `spring-boot-starter-web` - API REST
- `spring-boot-starter-data-jpa` - Persistencia de datos
- `postgresql` - Driver de PostgreSQL
- `lombok` - Reducción de código boilerplate
- `spring-boot-starter-validation` - Validación de datos

## ⚙️ Configuración

### Variables de Entorno
```properties
SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/habilis_db
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=admin123
```

### Base de Datos
- **Host**: db (en Docker) / localhost (local)
- **Puerto**: 5432
- **Base de datos**: habilis_db
- **Usuario**: postgres
- **Contraseña**: admin123

## 🏃 Ejecutar

### Con Docker Compose (Recomendado)
```bash
cd habilis
docker-compose up --build
```

### Localmente con Maven
```bash
cd backend-api
mvn clean install
mvn spring-boot:run
```

## 🔍 Endpoints Disponibles

### Health Check
```
GET http://localhost:8080/api/health
```
Respuesta:
```json
{
  "status": "UP",
  "service": "Habilis API",
  "timestamp": "2026-01-04T18:42:00",
  "message": "API funcionando correctamente"
}
```

### Welcome
```
GET http://localhost:8080/api/welcome
```
Respuesta:
```json
{
  "message": "¡Bienvenido a Habilis API!",
  "version": "1.0.0",
  "documentation": "http://localhost:8080/api/health"
}
```

## 📁 Estructura del Proyecto
```
backend-api/
├── src/
│   └── main/
│       ├── java/com/habilis/api/
│       │   ├── HabilisApplication.java    # Clase principal
│       │   ├── controller/                # Controladores REST
│       │   │   └── HealthController.java
│       │   ├── entity/                    # Entidades JPA
│       │   └── repository/                # Repositorios JPA
│       └── resources/
│           └── application.properties     # Configuración
├── Dockerfile
└── pom.xml
```

## 🔧 Características
- ✅ Configuración CORS habilitada para frontend
- ✅ Conexión a PostgreSQL configurada
- ✅ JPA/Hibernate con estrategia `update`
- ✅ Logging SQL habilitado para desarrollo
- ✅ Validación de datos
- ✅ Health check endpoints

## 📝 Notas
- El esquema de base de datos se actualiza automáticamente (`ddl-auto=update`)
- Los logs SQL están habilitados para facilitar el desarrollo
- CORS configurado para `http://localhost` y `http://localhost:80`
