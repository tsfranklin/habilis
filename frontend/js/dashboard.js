// ========================================
// CONFIGURACIÓN Y UTILIDADES
// ========================================

const API_BASE_URL = 'http://localhost:8080/api';

// Check auth on page load
document.addEventListener('DOMContentLoaded', () => {
    checkAuth();
    loadUserData();
    loadStats();
    load2FAStatus();
});

// ========================================
// AUTH CHECK
// ========================================

async function checkAuth() {
    try {
        const response = await fetch(`${API_BASE_URL}/auth/me`, {
            credentials: 'include'
        });

        if (!response.ok) {
            window.location.href = 'login.html';
            return;
        }

        const userData = await response.json();
        localStorage.setItem('currentUser', JSON.stringify(userData));

        // Update UI with user name
        document.getElementById('userName').textContent = userData.nombreCompleto;
    } catch (error) {
        console.error('Auth check error:', error);
        window.location.href = 'login.html';
    }
}

// ========================================
// LOAD USER DATA
// ========================================

async function loadUserData() {
    try {
        const userData = JSON.parse(localStorage.getItem('currentUser'));
        if (!userData) return;

        // Update profile section
        document.getElementById('profileName').textContent = userData.nombreCompleto;
        document.getElementById('profileEmail').textContent = userData.correoElectronico;
        document.getElementById('profilePhone').textContent = userData.movil;
        document.getElementById('profileType').textContent = userData.tipoUsuario;
        document.getElementById('profileStatus').textContent = userData.cuentaActiva ? 'Activa' : 'Inactiva';
        document.getElementById('profileStatus').className = userData.cuentaActiva ? 'badge badge-success' : 'badge badge-danger';
    } catch (error) {
        console.error('Error loading user data:', error);
    }
}

// ========================================
// LOAD STATS
// ========================================

async function loadStats() {
    try {
        const response = await fetch(`${API_BASE_URL}/pedidos/mis-estadisticas`, {
            credentials: 'include'
        });

        if (!response.ok) throw new Error('Failed to load stats');

        const stats = await response.json();

        document.getElementById('totalOrders').textContent = stats.totalPedidos;
        document.getElementById('pendingOrders').textContent = stats.pedidosPendientes;
        document.getElementById('completedOrders').textContent = stats.pedidosCompletados;
        document.getElementById('totalSpent').textContent = `€${stats.totalGastado.toFixed(2)}`;
    } catch (error) {
        console.error('Error loading stats:', error);
    }
}

// =======================================
// SECTION NAVIGATION
// ========================================

function showSection(sectionName) {
    // Hide all sections
    document.querySelectorAll('.dashboard-section').forEach(section => {
        section.style.display = 'none';
    });

    // Remove active class from all links
    document.querySelectorAll('.sidebar-link').forEach(link => {
        link.classList.remove('active');
    });

    // Show selected section
    document.getElementById(`${sectionName}-section`).style.display = 'block';

    // Add active class to clicked link
    event.target.classList.add('active');

    // Load section-specific data
    if (sectionName === 'orders') {
        loadOrders();
    }
}

// ========================================
// LOAD ORDERS
// ========================================

async function loadOrders() {
    const container = document.getElementById('ordersContainer');
    container.innerHTML = '<div class="loading">Cargando pedidos...</div>';

    try {
        const response = await fetch(`${API_BASE_URL}/pedidos`, {
            credentials: 'include'
        });

        if (!response.ok) throw new Error('Failed to load orders');

        const orders = await response.json();

        if (orders.length === 0) {
            container.innerHTML = '<p class="empty-message">No tienes pedidos aún. <a href="catalog.html">¡Explora nuestro catálogo!</a></p>';
            return;
        }

        container.innerHTML = orders.map(order => `
            <div class="order-card">
                <div class="order-header">
                    <div>
                        <h3>Pedido #${order.id}</h3>
                        <p class="order-date">${new Date(order.fechaPedido).toLocaleDateString('es-ES')}</p>
                    </div>
                    <div>
                        <span class="order-status badge badge-${getStatusClass(order.estado)}">${order.estado}</span>
                        <span class="order-total">€${order.totalPedido.toFixed(2)}</span>
                    </div>
                </div>
                <div class="order-items">
                    ${order.detalles.map(detail => `
                        <div class="order-item">
                            <span>${detail.producto.nombre}</span>
                            <span>${detail.cantidad}x €${detail.precioUnitario.toFixed(2)}</span>
                        </div>
                    `).join('')}
                </div>
                <div class="order-actions">
                    <button onclick="downloadInvoice(${order.id})" class="btn btn-primary btn-sm">
                        📄 Descargar Factura
                    </button>
                    ${order.estado === 'PENDIENTE' ? `
                        <button onclick="cancelOrder(${order.id})" class="btn btn-danger btn-sm">Cancelar Pedido</button>
                    ` : ''}
                </div>
            </div>
        `).join('');
    } catch (error) {
        console.error('Error loading orders:', error);
        container.innerHTML = '<p class="error-message">Error al cargar pedidos</p>';
    }
}

function getStatusClass(status) {
    const classes = {
        'PENDIENTE': 'warning',
        'ENVIADO': 'info',
        'COMPLETADO': 'success',
        'CANCELADO': 'danger'
    };
    return classes[status] || 'secondary';
}

async function cancelOrder(orderId) {
    if (!confirm('¿Estás seguro de que quieres cancelar este pedido?')) return;

    try {
        const response = await fetch(`${API_BASE_URL}/pedidos/${orderId}/cancelar`, {
            method: 'POST',
            credentials: 'include'
        });

        const data = await response.json();

        if (data.success) {
            alert('Pedido cancelado exitosamente');
            loadOrders();
            loadStats();
        } else {
            alert(data.error || 'Error al cancelar pedido');
        }
    } catch (error) {
        console.error('Error canceling order:', error);
        alert('Error al cancelar pedido');
    }
}

// ========================================
// 2FA MANAGEMENT
// ========================================

async function load2FAStatus() {
    try {
        const userData = JSON.parse(localStorage.getItem('currentUser'));
        const is2FAEnabled = userData.twoFactorEnabled;

        const statusBadge = document.getElementById('2faStatus');
        const description = document.getElementById('2faDescription');

        if (is2FAEnabled) {
            statusBadge.textContent = 'Activado';
            statusBadge.className = 'badge badge-success';
            description.textContent = 'Tu cuenta está protegida con autenticación de dos factores.';
            document.getElementById('enable2faContainer').style.display = 'none';
            document.getElementById('disable2faContainer').style.display = 'block';
        } else {
            statusBadge.textContent = 'Desactivado';
            statusBadge.className = 'badge badge-warning';
            description.textContent = 'Aumenta la seguridad de tu cuenta activando la autenticación de dos factores.';
            document.getElementById('enable2faContainer').style.display = 'block';
            document.getElementById('disable2faContainer').style.display = 'none';
        }
    } catch (error) {
        console.error('Error loading 2FA status:', error);
    }
}

async function enable2FA() {
    try {
        const response = await fetch(`${API_BASE_URL}/auth/2fa/habilitar`, {
            method: 'POST',
            credentials: 'include'
        });

        const data = await response.json();

        if (data.success) {
            document.getElementById('qrCodeImage').src = data.qrCode;
            document.getElementById('qrCodeContainer').style.display = 'block';
            document.getElementById('enable2faContainer').style.display = 'none';
        } else {
            show2FAMessage(data.error || 'Error al generar código QR', 'error');
        }
    } catch (error) {
        console.error('Error enabling 2FA:', error);
        show2FAMessage('Error de conexión', 'error');
    }
}

async function verify2FAActivation() {
    const code = document.getElementById('verify2faCode').value;

    if (!code || code.length !== 6) {
        show2FAMessage('Ingresa un código de 6 dígitos', 'error');
        return;
    }

    try {
        const response = await fetch(`${API_BASE_URL}/auth/2fa/verificar-activacion?codigo=${code}`, {
            method: 'POST',
            credentials: 'include'
        });

        const data = await response.json();

        if (data.success) {
            show2FAMessage('2FA activado exitosamente', 'success');
            document.getElementById('qrCodeContainer').style.display = 'none';

            // Reload user data
            await checkAuth();
            load2FAStatus();
        } else {
            show2FAMessage(data.message || 'Código incorrecto', 'error');
        }
    } catch (error) {
        console.error('Error verifying 2FA:', error);
        show2FAMessage('Error de conexión', 'error');
    }
}

async function disable2FA() {
    const password = document.getElementById('disablePassword').value;

    if (!password) {
        show2FAMessage('Ingresa tu contraseña', 'error');
        return;
    }

    if (!confirm('¿Estás seguro de que quieres desactivar la autenticación de dos factores?')) return;

    try {
        const response = await fetch(`${API_BASE_URL}/auth/2fa/deshabilitar?contrasena=${encodeURIComponent(password)}`, {
            method: 'POST',
            credentials: 'include'
        });

        const data = await response.json();

        if (data.success) {
            show2FAMessage('2FA desactivado exitosamente', 'success');
            document.getElementById('disablePassword').value = '';

            // Reload user data
            await checkAuth();
            load2FAStatus();
        } else {
            show2FAMessage(data.message || 'Error al desactivar 2FA', 'error');
        }
    } catch (error) {
        console.error('Error disabling 2FA:', error);
        show2FAMessage('Error de conexión', 'error');
    }
}

function show2FAMessage(message, type) {
    const element = document.getElementById('2faMessage');
    element.textContent = message;
    element.className = `alert alert-${type}`;
    element.style.display = 'block';

    setTimeout(() => {
        element.style.display = 'none';
    }, 5000);
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

        localStorage.removeItem('currentUser');
        window.location.href = 'login.html';
    } catch (error) {
        console.error('Logout error:', error);
        window.location.href = 'login.html';
    }
}
