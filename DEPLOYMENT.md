# 🚀 Guía de Despliegue - HÁBILIS

Esta guía te ayudará a lanzar el proyecto HÁBILIS completo usando Docker Compose.

---

## 📋 Requisitos Previos

Antes de comenzar, asegúrate de tener instalado:

- **Docker Desktop** (Windows/Mac) o **Docker Engine** (Linux)
- **Docker Compose** (incluido en Docker Desktop)

### Verificar instalación:

```powershell
docker --version
docker-compose --version
```

---

## 🎯 Opción 1: Despliegue Rápido (Recomendado)

### Paso 1: Navegar al directorio del proyecto

```powershell
cd C:\Users\frank\OneDrive\Desktop\PROYECTO_HABILIS\habilis
```

### Paso 2: Lanzar todos los servicios

```powershell
docker-compose up --build
```

**¿Qué hace este comando?**
- `docker-compose up`: Inicia todos los servicios definidos en `docker-compose.yml`
- `--build`: Construye las imágenes antes de iniciar (necesario la primera vez o tras cambios)

### Paso 3: Esperar a que los servicios estén listos

Verás logs como estos:

```
✓ Habilis API iniciada correctamente
✓ Puerto: 8080
✓ Documentación: http://localhost:8080
```

### Paso 4: Acceder a la aplicación

- **Frontend**: http://localhost
- **Backend API**: http://localhost:8080
- **PostgreSQL**: localhost:5432

---

## 🔧 Opción 2: Despliegue Paso a Paso

Si prefieres más control, puedes lanzar los servicios uno por uno:

### 1. Crear la red Docker

```powershell
docker network create red-habilis
```

### 2. Lanzar PostgreSQL

```powershell
docker run -d `
  --name habilis_db `
  --network red-habilis `
  -e POSTGRES_USER=postgres `
  -e POSTGRES_PASSWORD=admin123 `
  -e POSTGRES_DB=habilis_db `
  -p 5432:5432 `
  -v postgres_data:/var/lib/postgresql/data `
  postgres:15
```

### 3. Construir y lanzar Backend

```powershell
cd backend-api
docker build -t habilis-backend .
docker run -d `
  --name habilis_api `
  --network red-habilis `
  -p 8080:8080 `
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://habilis_db:5432/habilis_db `
  -e SPRING_DATASOURCE_USERNAME=postgres `
  -e SPRING_DATASOURCE_PASSWORD=admin123 `
  habilis-backend
```

### 4. Construir y lanzar Frontend

```powershell
cd ../frontend
docker build -t habilis-frontend .
docker run -d `
  --name habilis_web `
  --network red-habilis `
  -p 80:80 `
  habilis-frontend
```

---

## 🧪 Verificar que Todo Funciona

### 1. Verificar contenedores activos

```powershell
docker ps
```

Deberías ver 3 contenedores:
- `habilis_db` (PostgreSQL)
- `habilis_api` (Spring Boot)
- `habilis_web` (Nginx)

### 2. Verificar logs del backend

```powershell
docker logs habilis_api
```

Busca el mensaje: `✓ Habilis API iniciada correctamente`

### 3. Probar endpoints desde navegador

Abre http://localhost y:
1. Navega a la sección "Probar API"
2. Haz clic en "Probar Endpoint" para `/api/health`
3. Haz clic en "Probar Endpoint" para `/api/welcome`

Deberías ver respuestas JSON exitosas.

### 4. Verificar base de datos

```powershell
docker exec -it habilis_db psql -U postgres -d habilis_db
```

Dentro de PostgreSQL:
```sql
\dt                    -- Ver tablas
\q                     -- Salir
```

---

## 🛑 Detener los Servicios

### Detener todos los servicios (mantiene datos)

```powershell
docker-compose stop
```

### Detener y eliminar contenedores (mantiene datos)

```powershell
docker-compose down
```

### Detener y eliminar TODO (incluye volúmenes de BD)

```powershell
docker-compose down -v
```

⚠️ **CUIDADO**: El último comando borrará todos los datos de la base de datos.

---

## 🔄 Reiniciar Servicios

### Reiniciar todos

```powershell
docker-compose restart
```

### Reiniciar solo el backend

```powershell
docker-compose restart backend
```

### Reiniciar solo el frontend

```powershell
docker-compose restart frontend
```

---

## 📊 Ver Logs en Tiempo Real

### Todos los servicios

```powershell
docker-compose logs -f
```

### Solo backend

```powershell
docker-compose logs -f backend
```

### Solo base de datos

```powershell
docker-compose logs -f db
```

Presiona `Ctrl+C` para salir de los logs.

---

## ❌ Solución de Problemas

### Problema: "Port 80 is already in use"

**Solución**: Otro servicio está usando el puerto 80 (probablemente IIS o Apache).

```powershell
# Detener IIS (si está instalado)
net stop was /y

# O cambiar el puerto en docker-compose.yml
# En la sección frontend, cambiar:
ports:
  - "8081:80"  # Ahora accede en http://localhost:8081
```

### Problema: "Port 8080 is already in use"

**Solución**: Cambiar el puerto del backend en `docker-compose.yml`:

```yaml
backend:
  ports:
    - "8081:8080"  # Ahora el backend está en http://localhost:8081
```

### Problema: Backend no conecta a la BD

**Solución**: Verificar que la BD esté lista antes de iniciar el backend.

Agregar en `docker-compose.yml` bajo el servicio `backend`:

```yaml
depends_on:
  db:
    condition: service_healthy
```

### Problema: Cambios en código no se reflejan

**Solución**: Reconstruir las imágenes:

```powershell
docker-compose up --build
```

---

## 🧹 Limpiar Docker (Liberar Espacio)

```powershell
# Eliminar contenedores detenidos
docker container prune

# Eliminar imágenes sin usar
docker image prune

# Eliminar TODO (contenedores, imágenes, volúmenes)
docker system prune -a --volumes
```

---

## 📝 Comandos Útiles

| Comando | Descripción |
|---------|-------------|
| `docker-compose up -d` | Iniciar en segundo plano (detached) |
| `docker-compose ps` | Ver estado de servicios |
| `docker-compose exec backend bash` | Entrar al contenedor backend |
| `docker-compose exec db psql -U postgres` | Conectar a PostgreSQL |
| `docker stats` | Ver uso de recursos en tiempo real |

---

## ✅ Checklist de Verificación

- [ ] Docker Desktop está ejecutándose
- [ ] Navegaste al directorio `habilis`
- [ ] Ejecutaste `docker-compose up --build`
- [ ] Viste el mensaje "Habilis API iniciada correctamente"
- [ ] Frontend accesible en http://localhost
- [ ] Backend responde en http://localhost:8080/api/health
- [ ] Endpoints de prueba funcionan correctamente
- [ ] Estado del sistema muestra todo "Online"

---

## 🎓 Próximos Pasos

Una vez que todo funcione:

1. **Implementar entidades JPA** (Usuario, Producto, Pedido, etc.)
2. **Crear controladores REST** para CRUD
3. **Implementar autenticación** con Spring Security
4. **Desarrollar frontend completo** con login y registro
5. **Generar informes PDF**

---

**¿Necesitas ayuda?** Revisa los logs con `docker-compose logs -f` para ver mensajes de error detallados.
