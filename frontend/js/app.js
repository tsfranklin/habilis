// ========================================
// HÁBILIS - App.js Profesional
// Lógica para index.html
// ========================================

const API_BASE_URL = '/api';

document.addEventListener('DOMContentLoaded', () => {
    verificarSesion();
});

// ========================================
// VERIFICAR SESIÓN Y MOSTRAR NAVBAR DINÁMICO
// ========================================

async function verificarSesion() {
    const authContainer = document.getElementById('authButtons');
    if (!authContainer) return; // Solo ejecutar en index.html

    try {
        const response = await fetch(`${API_BASE_URL}/auth/me`, {
            credentials: 'include'
        });

        if (response.ok) {
            const user = await response.json();

            // Usuario logueado: Mostrar saludo y botón salir
            const primerNombre = user.nombreCompleto.split(' ')[0];

            authContainer.innerHTML = `
                <a href="${user.tipoUsuario === 'ADMIN' ? 'admin-dashboard.html' : 'user-dashboard.html'}" class="btn-text">
                    Hola, ${primerNombre}
                </a>
                <button onclick="logout()" class="btn btn-secondary btn-sm">Salir</button>
            `;

            // Si es admin, agregar botón extra
            if (user.tipoUsuario === 'ADMIN') {
                const adminBtn = document.createElement('a');
                adminBtn.href = 'admin-dashboard.html';
                adminBtn.className = 'btn btn-primary btn-sm';
                adminBtn.textContent = 'Panel Admin';
                adminBtn.style.marginLeft = '8px';
                authContainer.appendChild(adminBtn);
            }
        }
        // Si NO está logueado, dejamos los botones por defecto del HTML
    } catch (error) {
        console.log('Visitante anónimo');
    }
}

// ========================================
// LOGOUT
// ========================================

async function logout() {
    try {
        await fetch(`${API_BASE_URL}/auth/logout`, {
            method: 'POST',
            credentials: 'include'
        });

        // Clear all session and local storage
        sessionStorage.clear();
        localStorage.removeItem('currentUser');
        localStorage.removeItem('userRole');
        console.log('Session data cleared on logout');

        window.location.reload();
    } catch (error) {
        console.error('Error al cerrar sesión:', error);
        // Even if logout fails on server, clear local data
        sessionStorage.clear();
        localStorage.removeItem('currentUser');
        localStorage.removeItem('userRole');
        window.location.reload();
    }
}

// ========================================
// HAMBURGER MENU (Mobile)
// ========================================

const hamburger = document.getElementById('hamburger');
const navMenu = document.getElementById('navMenu');

if (hamburger && navMenu) {
    hamburger.addEventListener('click', () => {
        navMenu.classList.toggle('active');
    });
}
