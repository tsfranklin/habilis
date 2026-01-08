// Configuración de la API
const API_URL = 'http://localhost:8080';

/**
 * Función para probar el endpoint /api/health
 */
async function testHealth() {
    const responseDiv = document.getElementById('health-response');
    responseDiv.textContent = 'Conectando...';
    responseDiv.className = 'response-box';
    responseDiv.style.display = 'block';

    try {
        const response = await fetch(`${API_URL}/api/health`);

        if (response.ok) {
            const data = await response.json();
            responseDiv.textContent = JSON.stringify(data, null, 2);
            responseDiv.className = 'response-box success';
            updateBackendStatus(true);
        } else {
            responseDiv.textContent = `Error: ${response.status} - ${response.statusText}`;
            responseDiv.className = 'response-box error';
            updateBackendStatus(false);
        }
    } catch (error) {
        responseDiv.textContent = `Error de conexión: ${error.message}\n\nAsegúrate de que el backend está ejecutándose en ${API_URL}`;
        responseDiv.className = 'response-box error';
        updateBackendStatus(false);
    }
}

/**
 * Función para probar el endpoint /api/welcome
 */
async function testWelcome() {
    const responseDiv = document.getElementById('welcome-response');
    responseDiv.textContent = 'Conectando...';
    responseDiv.className = 'response-box';
    responseDiv.style.display = 'block';

    try {
        const response = await fetch(`${API_URL}/api/welcome`);

        if (response.ok) {
            const data = await response.json();
            responseDiv.textContent = JSON.stringify(data, null, 2);
            responseDiv.className = 'response-box success';
            updateBackendStatus(true);
        } else {
            responseDiv.textContent = `Error: ${response.status} - ${response.statusText}`;
            responseDiv.className = 'response-box error';
            updateBackendStatus(false);
        }
    } catch (error) {
        responseDiv.textContent = `Error de conexión: ${error.message}\n\nAsegúrate de que el backend está ejecutándose en ${API_URL}`;
        responseDiv.className = 'response-box error';
        updateBackendStatus(false);
    }
}

/**
 * Actualizar estado del backend en el panel de sistema
 */
function updateBackendStatus(isOnline) {
    const backendStatus = document.getElementById('backend-status');
    const dbStatus = document.getElementById('db-status');

    if (isOnline) {
        backendStatus.textContent = '● Online';
        backendStatus.className = 'status-value status-online';
        // Si el backend está online, asumimos que la BD también
        dbStatus.textContent = '● Online';
        dbStatus.className = 'status-value status-online';
    } else {
        backendStatus.textContent = '● Offline';
        backendStatus.className = 'status-value status-offline';
        dbStatus.textContent = '● Desconocido';
        dbStatus.className = 'status-value status-offline';
    }
}

/**
 * Verificar estado del sistema al cargar la página
 */
async function checkSystemStatus() {
    try {
        const response = await fetch(`${API_URL}/api/health`);
        updateBackendStatus(response.ok);
    } catch (error) {
        updateBackendStatus(false);
    }
}

/**
 * Smooth scroll para navegación
 */
document.addEventListener('DOMContentLoaded', () => {
    // Verificar estado del sistema
    checkSystemStatus();

    // Smooth scroll para los enlaces de navegación
    document.querySelectorAll('a[href^="#"]').forEach(anchor => {
        anchor.addEventListener('click', function (e) {
            e.preventDefault();
            const target = document.querySelector(this.getAttribute('href'));
            if (target) {
                target.scrollIntoView({
                    behavior: 'smooth',
                    block: 'start'
                });
            }
        });
    });

    console.log('🧩 HÁBILIS Frontend cargado correctamente');
    console.log('📡 API URL:', API_URL);
});
