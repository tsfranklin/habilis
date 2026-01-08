# 🧩 HÁBILIS: Plataforma Educativa

**HÁBILIS** es una plataforma web de comercio electrónico y gestión de suscripciones educativas, diseñada para combatir el deterioro cognitivo y la dependencia digital en la infancia mediante la revalorización del juego tangible.

---

## 1. El Propósito (La Misión) 🎯
El proyecto nace como respuesta a la crisis de atención y desarrollo psicomotriz derivada de la sobreexposición a pantallas (teléfonos y tabletas) en niños de 3 a 13 años.

Su objetivo es **"desconectar para reconectar"**, proporcionando herramientas físicas que recuperan habilidades críticas como:
* La motricidad fina.
* La concentración profunda ("Deep Work").
* La creatividad manual.

## 2. El Modelo de Negocio 📦
Funciona bajo un modelo de **suscripción anual con entregas mensuales recurrentes**.

La plataforma web actúa como el centro de mando donde los padres (usuarios):
1.  Gestionan sus perfiles.
2.  Seleccionan el itinerario educativo adecuado según la edad de sus hijos (**segmentación por etapas evolutivas**).
3.  Reciben en sus hogares kits físicos (**"Misiones"**) con materiales didácticos exclusivos, manualidades y retos de ingeniería básica.

## 3. El Alcance Técnico 🛠️
Tecnológicamente, HÁBILIS es una aplicación web Full-Stack desarrollada bajo una arquitectura **MVC (Modelo-Vista-Controlador)** robusta y escalable.

* **Backend:** Implementado con **Java** y **Spring Boot**, exponiendo una **API REST** segura y eficiente.
* **Persistencia:** Utiliza **PostgreSQL** como motor de base de datos relacional, gestionada a través de **JPA/Hibernate** para la abstracción de datos.
* **Despliegue:** Todo el ecosistema está contenerizado mediante **Docker** y orquestado con **Docker Compose**, garantizando que la aplicación sea agnóstica al entorno y fácil de desplegar con un solo comando.

---

### 📝 Resumen (Elevator Pitch)
> "HÁBILIS es el 'Netflix' de las manualidades, pero en lugar de dejar a los niños pegados a una pantalla viendo series, usamos la web para enviarles cada mes una caja física con herramientas y juegos reales. Usamos la tecnología para sacarles de la tecnología."

---

## 🚀 Cómo ejecutar el proyecto

Para desplegar la aplicación completa (Base de datos + API + Web), ejecuta en la terminal:

```bash
docker compose up --build
```

La aplicación estará disponible en:
- **Frontend**: http://localhost
- **Backend API**: http://localhost:8080
- **Base de Datos**: localhost:5432

---

## 📁 Estructura del Proyecto

```
habilis/
├── docker-compose.yml           # Orquestador de servicios
├── backend-api/                 # API REST con Spring Boot
│   ├── Dockerfile
│   ├── pom.xml
│   ├── README.md
│   └── src/
│       └── main/
│           ├── java/com/habilis/api/
│           │   ├── HabilisApplication.java
│           │   ├── controller/
│           │   ├── entity/
│           │   └── repository/
│           └── resources/
│               └── application.properties
└── frontend/                    # Interfaz web estática
    ├── Dockerfile
    ├── index.html
    ├── css/
    │   └── styles.css
    └── js/
        └── app.js
```

---

## 🔧 Tecnologías Utilizadas

### Backend
- **Java 17**
- **Spring Boot 3.2.0**
- **Spring Data JPA**
- **PostgreSQL Driver**
- **Lombok**
- **Maven**

### Base de Datos
- **PostgreSQL 15**

### Frontend
- **HTML5**
- **CSS3** (Vanilla CSS con diseño moderno)
- **JavaScript** (ES6+)
- **Nginx** (Servidor web)

### DevOps
- **Docker**
- **Docker Compose**

---

## 📊 Servicios Docker

### 1. Base de Datos (PostgreSQL)
- **Container**: `habilis_db`
- **Puerto**: 5432
- **Base de datos**: `habilis_db`
- **Usuario**: `postgres`
- **Contraseña**: `admin123`

### 2. Backend API (Spring Boot)
- **Container**: `habilis_api`
- **Puerto**: 8080
- **Endpoints**:
  - `GET /api/health` - Health check
  - `GET /api/welcome` - Bienvenida

### 3. Frontend Web (Nginx)
- **Container**: `habilis_web`
- **Puerto**: 80
- **Acceso**: http://localhost

---

## 🎯 Próximos Pasos

1. **Desarrollo de Entidades**: Crear modelos de Usuario, Suscripción, Misión, etc.
2. **Sistema de Autenticación**: Implementar login y registro de usuarios
3. **Gestión de Suscripciones**: CRUD completo de suscripciones
4. **Panel de Administración**: Dashboard para gestionar contenido
5. **Pasarela de Pago**: Integración con Stripe/PayPal
6. **Sistema de Envíos**: Tracking de kits mensuales

---

## 👥 Público Objetivo

- **Usuarios Primarios**: Padres de niños entre 3-13 años
- **Beneficiarios**: Niños que necesitan reducir tiempo de pantalla
- **Segmentación**: Por etapas evolutivas del desarrollo infantil

---

## 💡 Valor Diferencial

✅ **Combate la adicción digital** mediante experiencias tangibles  
✅ **Desarrollo cognitivo** a través del juego físico  
✅ **Suscripción recurrente** con entregas mensuales personalizadas  
✅ **Contenido curado** por expertos en desarrollo infantil  
✅ **Tecnología al servicio del desapego tecnológico**

---

## 📄 Licencia

Proyecto educativo - Todos los derechos reservados © 2026 HÁBILIS
