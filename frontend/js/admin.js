// ========================================
// CONFIGURACIÓN
// ========================================

const API_BASE = '/api';
let editandoProductoId = null;
let editandoCategoriaId = null;

// ========================================
// INICIALIZACIÓN
// ========================================

document.addEventListener('DOMContentLoaded', () => {
    verificarPermisos();
    cargarCategorias();
    cargarProductos();

    // Event listeners para formularios
    document.getElementById('productoForm').addEventListener('submit', guardarProducto);
    document.getElementById('categoriaForm').addEventListener('submit', guardarCategoria);
});

// ========================================
// VERIFICACIÓN DE PERMISOS
// ========================================

async function verificarPermisos() {
    try {
        const response = await fetch(`${API_BASE}/auth/me`, {
            credentials: 'include'
        });

        if (!response.ok) {
            window.location.href = 'login.html';
            return;
        }

        const userData = await response.json();

        if (userData.tipoUsuario !== 'ADMIN') {
            alert('Acceso denegado. Se requieren permisos de administrador.');
            window.location.href = 'catalog.html';
        }
    } catch (error) {
        console.error('Error verificando permisos:', error);
        window.location.href = 'login.html';
    }
}

// ========================================
// NAVEGACIÓN ENTRE SECCIONES
// ========================================

function mostrarSeccion(seccion) {
    // Ocultar todas las secciones
    document.querySelectorAll('section[id^="sec-"]').forEach(s => {
        s.style.display = 'none';
    });

    // Mostrar la sección seleccionada
    document.getElementById(`sec-${seccion}`).style.display = 'block';

    // Cargar datos según la sección
    if (seccion === 'productos') {
        cargarCategoriasFiltro();
        cargarProductos();
    } else if (seccion === 'categorias') {
        cargarCategorias();
    } else if (seccion === 'pedidos') {
        cargarPedidosAdmin();
    }
}

// ========================================
// GESTIÓN DE CATEGORÍAS
// ========================================

async function cargarCategorias() {
    const tbody = document.getElementById('tablaCategorias');

    try {
        const response = await fetch(`${API_BASE}/categorias`);
        const categorias = await response.json();

        if (categorias.length === 0) {
            tbody.innerHTML = '<tr><td colspan="5" class="text-center">No hay categorías</td></tr>';
            return;
        }

        tbody.innerHTML = categorias.map(cat => `
            <tr>
                <td>${cat.id}</td>
                <td>${cat.nombre}</td>
                <td>${cat.descripcion || '-'}</td>
                <td>
                    <span class="badge badge-info" id="count-${cat.id}">-</span>
                </td>
                <td>
                    <button class="btn btn-secondary" onclick="editarCategoria(${cat.id})">Editar</button>
                    <button class="btn btn-danger" onclick="borrarCategoria(${cat.id})">Borrar</button>
                </td>
            </tr>
        `).join('');

        // Cargar conteo de productos por categoría
        categorias.forEach(async cat => {
            try {
                const countRes = await fetch(`${API_BASE}/categorias/${cat.id}/productos/count`);
                const data = await countRes.json();
                const badge = document.getElementById(`count-${cat.id}`);
                if (badge) {
                    badge.textContent = `${data.cantidadProductos} productos`;
                }
            } catch (e) {
                console.error('Error cargando conteo:', e);
            }
        });
    } catch (error) {
        console.error('Error cargando categorías:', error);
        tbody.innerHTML = '<tr><td colspan="5" class="error-message">Error al cargar categorías</td></tr>';
    }
}

async function guardarCategoria(e) {
    e.preventDefault();

    const id = document.getElementById('catId').value;
    const data = {
        nombre: document.getElementById('catNombre').value,
        descripcion: document.getElementById('catDesc').value
    };

    try {
        const url = id ? `${API_BASE}/categorias/${id}` : `${API_BASE}/categorias`;
        const method = id ? 'PUT' : 'POST';

        const response = await fetch(url, {
            method: method,
            headers: { 'Content-Type': 'application/json' },
            credentials: 'include',
            body: JSON.stringify(data)
        });

        const result = await response.json();

        if (result.success || response.ok) {
            alert(id ? 'Categoría actualizada' : 'Categoría creada');
            cancelarEdicionCategoria();
            cargarCategorias();
            cargarCategoriasFiltro();
        } else {
            alert(result.error || 'Error al guardar categoría');
        }
    } catch (error) {
        console.error('Error guardando categoría:', error);
        alert('Error de conexión');
    }
}

async function editarCategoria(id) {
    try {
        const response = await fetch(`${API_BASE}/categorias/${id}`);
        const categoria = await response.json();

        document.getElementById('catId').value = categoria.id;
        document.getElementById('catNombre').value = categoria.nombre;
        document.getElementById('catDesc').value = categoria.descripcion || '';
        document.getElementById('btnTextoCategoria').textContent = 'Actualizar Categoría';
        document.getElementById('btnCancelarCategoria').style.display = 'inline-block';

        editandoCategoriaId = id;

        // Scroll al formulario
        document.getElementById('categoriaForm').scrollIntoView({ behavior: 'smooth' });
    } catch (error) {
        console.error('Error cargando categoría:', error);
        alert('Error al cargar categoría');
    }
}

async function borrarCategoria(id) {
    // REQUISITO CRÍTICO: Borrado seguro con confirmación de texto
    const confirmacion = prompt('⚠️ ADVERTENCIA: Esta acción es irreversible.\\n\\nPara borrar esta categoría, escribe exactamente la palabra: BORRAR');

    if (confirmacion !== 'BORRAR') {
        if (confirmacion !== null) {
            alert('Texto incorrecto. Operación cancelada.');
        }
        return;
    }

    try {
        const response = await fetch(`${API_BASE}/categorias/${id}`, {
            method: 'DELETE',
            credentials: 'include'
        });

        const data = await response.json();

        if (data.success || response.ok) {
            alert('Categoría eliminada exitosamente');
            cargarCategorias();
        } else {
            alert(data.error || 'Error: No se puede borrar (tiene productos asociados)');
        }
    } catch (error) {
        console.error('Error borrando categoría:', error);
        alert('Error de conexión');
    }
}

function cancelarEdicionCategoria() {
    document.getElementById('categoriaForm').reset();
    document.getElementById('catId').value = '';
    document.getElementById('btnTextoCategoria').textContent = 'Crear Categoría';
    document.getElementById('btnCancelarCategoria').style.display = 'none';
    editandoCategoriaId = null;
}

// ========================================
// GESTIÓN DE PRODUCTOS
// ========================================

async function cargarCategoriasFiltro() {
    try {
        const response = await fetch(`${API_BASE}/categorias`);
        const categorias = await response.json();

        const select = document.getElementById('prodCategoria');
        select.innerHTML = '<option value="" disabled selected>Seleccione categoría...</option>' +
            categorias.map(cat => `<option value="${cat.id}">${cat.nombre}</option>`).join('');
    } catch (error) {
        console.error('Error cargando categorías para filtro:', error);
    }
}

async function cargarProductos() {
    const tbody = document.getElementById('tablaProductos');

    try {
        const response = await fetch(`${API_BASE}/productos`);
        const productos = await response.json();

        if (productos.length === 0) {
            tbody.innerHTML = '<tr><td colspan="6" class="text-center">No hay productos</td></tr>';
            return;
        }

        tbody.innerHTML = productos.map(prod => `
            <tr>
                <td>${prod.id}</td>
                <td>${prod.nombre}</td>
                <td>${prod.categoria ? prod.categoria.nombre : '-'}</td>
                <td>€${prod.precio.toFixed(2)}</td>
                <td>
                    <span class="badge ${prod.stock > 10 ? 'badge-success' : prod.stock > 0 ? 'badge-warning' : 'badge-danger'}">
                        ${prod.stock}
                    </span>
                </td>
                <td>
                    <button class="btn btn-secondary" onclick="editarProducto(${prod.id})">Editar</button>
                    <button class="btn btn-danger" onclick="borrarProducto(${prod.id})">Borrar</button>
                </td>
            </tr>
        `).join('');
    } catch (error) {
        console.error('Error cargando productos:', error);
        tbody.innerHTML = '<tr><td colspan="6" class="error-message">Error al cargar productos</td></tr>';
    }
}

async function guardarProducto(e) {
    e.preventDefault();

    const id = document.getElementById('prodId').value;
    const data = {
        categoriaId: parseInt(document.getElementById('prodCategoria').value),
        nombre: document.getElementById('prodNombre').value,
        descripcion: document.getElementById('prodDesc').value,
        precio: parseFloat(document.getElementById('prodPrecio').value),
        stock: parseInt(document.getElementById('prodStock').value),
        imagenUrl: document.getElementById('prodImagen').value || '📦'
    };

    try {
        const url = id ? `${API_BASE}/productos/${id}` : `${API_BASE}/productos`;
        const method = id ? 'PUT' : 'POST';

        const response = await fetch(url, {
            method: method,
            headers: { 'Content-Type': 'application/json' },
            credentials: 'include',
            body: JSON.stringify(data)
        });

        const result = await response.json();

        if (result.success || response.ok) {
            alert(id ? 'Producto actualizado' : 'Producto creado');
            cancelarEdicion();
            cargarProductos();
        } else {
            alert(result.error || 'Error al guardar producto');
        }
    } catch (error) {
        console.error('Error guardando producto:', error);
        alert('Error de conexión');
    }
}

async function editarProducto(id) {
    try {
        const response = await fetch(`${API_BASE}/productos/${id}`);
        const producto = await response.json();

        document.getElementById('prodId').value = producto.id;
        document.getElementById('prodNombre').value = producto.nombre;
        document.getElementById('prodDesc').value = producto.descripcion;
        document.getElementById('prodPrecio').value = producto.precio;
        document.getElementById('prodStock').value = producto.stock;
        document.getElementById('prodImagen').value = producto.imagenUrl || '';
        document.getElementById('prodCategoria').value = producto.categoria.id;
        document.getElementById('btnTexto').textContent = 'Actualizar Producto';
        document.getElementById('btnCancelar').style.display = 'inline-block';

        editandoProductoId = id;

        // Scroll al formulario
        document.getElementById('productoForm').scrollIntoView({ behavior: 'smooth' });
    } catch (error) {
        console.error('Error cargando producto:', error);
        alert('Error al cargar producto');
    }
}

async function borrarProducto(id) {
    // REQUISITO CRÍTICO: Borrado seguro con confirmación de texto
    const confirmacion = prompt('⚠️ ADVERTENCIA: Esta acción es irreversible.\\n\\nPara borrar este producto permanentemente, escribe exactamente la palabra: BORRAR');

    if (confirmacion !== 'BORRAR') {
        if (confirmacion !== null) {
            alert('Texto incorrecto. Operación cancelada.');
        }
        return;
    }

    try {
        const response = await fetch(`${API_BASE}/productos/${id}`, {
            method: 'DELETE',
            credentials: 'include'
        });

        const data = await response.json();

        if (data.success || response.ok) {
            alert('Producto eliminado exitosamente');
            cargarProductos();
        } else {
            alert(data.error || 'Error al eliminar producto');
        }
    } catch (error) {
        console.error('Error borrando producto:', error);
        alert('Error de conexión');
    }
}

function cancelarEdicion() {
    document.getElementById('productoForm').reset();
    document.getElementById('prodId').value = '';
    document.getElementById('btnTexto').textContent = 'Crear Producto';
    document.getElementById('btnCancelar').style.display = 'none';
    editandoProductoId = null;
}

// ========================================
// GESTIÓN DE PEDIDOS (ADMIN)
// ========================================

async function cargarPedidosAdmin() {
    const container = document.getElementById('listaPedidos');
    const estado = document.getElementById('filtroEstado').value;

    try {
        const url = estado ? `${API_BASE}/pedidos/estado/${estado}` : `${API_BASE}/pedidos`;
        const response = await fetch(url, { credentials: 'include' });
        const pedidos = await response.json();

        if (pedidos.length === 0) {
            container.innerHTML = '<p class="empty-message">No hay pedidos</p>';
            return;
        }

        container.innerHTML = pedidos.map(pedido => `
            <div class="order-card" style="margin-bottom: 1rem;">
                <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1rem;">
                    <div>
                        <h3>Pedido #${pedido.id}</h3>
                        <p>Cliente: ${pedido.usuario.nombreCompleto} (${pedido.usuario.correoElectronico})</p>
                        <p>Fecha: ${new Date(pedido.fechaPedido).toLocaleString('es-ES')}</p>
                    </div>
                    <div style="text-align: right;">
                        <span class="badge badge-${getStatusClass(pedido.estado)}">${pedido.estado}</span>
                        <p style="font-size: 1.5rem; color: var(--secondary-color); font-weight: bold;">€${pedido.totalPedido.toFixed(2)}</p>
                    </div>
                </div>
                <div style="background: var(--bg-color); padding: 1rem; border-radius: var(--radius); margin-bottom: 1rem;">
                    ${pedido.detalles.map(det => `
                        <div style="display: flex; justify-content: space-between; padding: 0.5rem 0; border-bottom: 1px solid #ddd;">
                            <span>${det.producto.nombre}</span>
                            <span>${det.cantidad}x €${det.precioUnitario.toFixed(2)} = €${(det.cantidad * det.precioUnitario).toFixed(2)}</span>
                        </div>
                    `).join('')}
                </div>
                ${pedido.estado === 'PENDIENTE' ? `
                    <button class="btn btn-success" onclick="cambiarEstadoPedido(${pedido.id}, 'ENVIADO')">Marcar como Enviado</button>
                ` : ''}
                ${pedido.estado === 'ENVIADO' ? `
                    <button class="btn btn-success" onclick="cambiarEstadoPedido(${pedido.id}, 'COMPLETADO')">Marcar como Completado</button>
                ` : ''}
                <button class="btn btn-secondary" onclick="verDetallePedido(${pedido.id})">Ver Detalle</button>
            </div>
        `).join('');
    } catch (error) {
        console.error('Error cargando pedidos:', error);
        container.innerHTML = '<p class="error-message">Error al cargar pedidos</p>';
    }
}

async function cambiarEstadoPedido(id, nuevoEstado) {
    if (!confirm(`¿Cambiar estado del pedido a ${nuevoEstado}?`)) return;

    try {
        const response = await fetch(`${API_BASE}/pedidos/${id}/estado?nuevoEstado=${nuevoEstado}`, {
            method: 'PATCH',
            credentials: 'include'
        });

        const data = await response.json();

        if (data.success) {
            alert('Estado actualizado');
            cargarPedidosAdmin();
        } else {
            alert(data.error || 'Error al cambiar estado');
        }
    } catch (error) {
        console.error('Error cambiando estado:', error);
        alert('Error de conexión');
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

// ========================================
// LOGOUT
// ========================================

async function logout() {
    try {
        await fetch(`${API_BASE}/auth/logout`, {
            method: 'POST',
            credentials: 'include'
        });

        // Clear all session storage
        sessionStorage.clear();
        console.log('Session data cleared on logout');

        window.location.href = 'login.html';
    } catch (error) {
        console.error('Logout error:', error);
        // Even if logout fails on server, clear local data
        sessionStorage.clear();
        window.location.href = 'login.html';
    }
}
