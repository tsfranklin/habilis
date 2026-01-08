// Configuración de la API
const API_URL = 'http://localhost:8080';

// Función para probar la conexión con el backend
async function testApiConnection() {
    const responseDiv = document.getElementById('response');
    responseDiv.textContent = 'Conectando...';
    responseDiv.className = '';
    
    try {
        const response = await fetch(`${API_URL}/api/health`);
        
        if (response.ok) {
            const data = await response.json();
            responseDiv.textContent = `✓ Conexión exitosa: ${JSON.stringify(data)}`;
            responseDiv.className = 'success';
        } else {
            responseDiv.textContent = `✗ Error: ${response.status} - ${response.statusText}`;
            responseDiv.className = 'error';
        }
    } catch (error) {
        responseDiv.textContent = `✗ Error de conexión: ${error.message}`;
        responseDiv.className = 'error';
    }
}

// Event Listeners
document.addEventListener('DOMContentLoaded', () => {
    const testButton = document.getElementById('testButton');
    
    if (testButton) {
        testButton.addEventListener('click', testApiConnection);
    }
});
