// ========================================
// CONFIGURACIÓN Y UTILIDADES
// ========================================

const API_BASE_URL = 'http://localhost:8080/api';

// Utilidad para mostrar/ocultar loaders
function showLoader(buttonElement) {
    const btnText = buttonElement.querySelector('.btn-text');
    const btnLoader = buttonElement.querySelector('.btn-loader');
    if (btnText) btnText.style.display = 'none';
    if (btnLoader) btnLoader.style.display = 'inline';
    buttonElement.disabled = true;
}

function hideLoader(buttonElement) {
    const btnText = buttonElement.querySelector('.btn-text');
    const btnLoader = buttonElement.querySelector('.btn-loader');
    if (btnText) btnText.style.display = 'inline';
    if (btnLoader) btnLoader.style.display = 'none';
    buttonElement.disabled = false;
}

// Utilidad para mostrar mensajes
function showMessage(elementId, message, type = 'error') {
    const element = document.getElementById(elementId);
    if (!element) return;

    element.textContent = message;
    element.className = `alert alert-${type}`;
    element.style.display = 'block';

    // Ocultar después de 5 segundos
    setTimeout(() => {
        element.style.display = 'none';
    }, 5000);
}

// ========================================
// LOGIN
// ========================================

document.getElementById('loginForm')?.addEventListener('submit', async (e) => {
    e.preventDefault();

    const form = e.target;
    const submitBtn = form.querySelector('button[type="submit"]');

    const email = document.getElementById('loginEmail').value;
    const password = document.getElementById('loginPassword').value;

    showLoader(submitBtn);

    try {
        const response = await fetch(`${API_BASE_URL}/auth/login`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            credentials: 'include', // Important for cookies
            body: JSON.stringify({
                correoElectronico: email,
                contrasena: password
            })
        });

        const data = await response.json();

        if (data.success) {
            // Check if 2FA is required
            if (data.requires2FA) {
                // Show 2FA form
                document.getElementById('loginForm').style.display = 'none';
                document.getElementById('twoFactorForm').style.display = 'block';
                document.getElementById('twoFactorUserId').value = data.userId;
                showMessage('loginSuccess', 'Ingresa tu código de autenticación', 'success');
            } else {
                // Login successful, redirect to dashboard
                showMessage('loginSuccess', 'Inicio de sesión exitoso', 'success');
                setTimeout(() => {
                    window.location.href = data.tipoUsuario === 'ADMIN' ? 'admin-dashboard.html' : 'user-dashboard.html';
                }, 1000);
            }
        } else {
            showMessage('loginError', data.message || 'Error al iniciar sesión');
        }
    } catch (error) {
        console.error('Login error:', error);
        showMessage('loginError', 'Error de conexión con el servidor');
    } finally {
        hideLoader(submitBtn);
    }
});

// ========================================
// 2FA VERIFICATION
// ========================================

document.getElementById('twoFactorForm')?.addEventListener('submit', async (e) => {
    e.preventDefault();

    const form = e.target;
    const submitBtn = form.querySelector('button[type="submit"]');

    const userId = document.getElementById('twoFactorUserId').value;
    const code = document.getElementById('twoFactorCode').value;

    showLoader(submitBtn);

    try {
        const response = await fetch(`${API_BASE_URL}/auth/verificar-2fa?usuarioId=${userId}&codigo=${code}`, {
            method: 'POST',
            credentials: 'include'
        });

        const data = await response.json();

        if (data.success) {
            showMessage('loginSuccess', 'Verificación exitosa', 'success');
            setTimeout(() => {
                window.location.href = data.tipoUsuario === 'ADMIN' ? 'admin-dashboard.html' : 'user-dashboard.html';
            }, 1000);
        } else {
            showMessage('twoFactorError', data.message || 'Código incorrecto');
        }
    } catch (error) {
        console.error('2FA verification error:', error);
        showMessage('twoFactorError', 'Error de conexión con el servidor');
    } finally {
        hideLoader(submitBtn);
    }
});

function cancel2FA() {
    document.getElementById('twoFactorForm').style.display = 'none';
    document.getElementById('loginForm').style.display = 'block';
    document.getElementById('twoFactorCode').value = '';
}

// ========================================
// REGISTRO
// ========================================

document.getElementById('registerForm')?.addEventListener('submit', async (e) => {
    e.preventDefault();

    const form = e.target;
    const submitBtn = form.querySelector('button[type="submit"]');

    const password = document.getElementById('registerPassword').value;
    const passwordConfirm = document.getElementById('registerPasswordConfirm').value;

    // Validate password match
    if (password !== passwordConfirm) {
        showMessage('registerError', 'Las contraseñas no coinciden');
        return;
    }

    const formData = {
        nombreCompleto: document.getElementById('registerName').value,
        correoElectronico: document.getElementById('registerEmail').value,
        movil: document.getElementById('registerPhone').value,
        contrasena: password
    };

    showLoader(submitBtn);

    try {
        const response = await fetch(`${API_BASE_URL}/auth/register`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(formData)
        });

        const data = await response.json();

        if (data.success) {
            showMessage('registerSuccess',
                'Cuenta creada exitosamente. Revisa tu correo para activar tu cuenta. ' +
                '(Nota: Si no configuraste SMTP, el token aparecerá en los logs del backend)',
                'success'
            );
            form.reset();

            // Redirect to confirmation page after 3 seconds
            setTimeout(() => {
                window.location.href = 'confirm-email.html';
            }, 3000);
        } else {
            showMessage('registerError', data.message || 'Error al registrar usuario');
        }
    } catch (error) {
        console.error('Register error:', error);
        showMessage('registerError', 'Error de conexión con el servidor');
    } finally {
        hideLoader(submitBtn);
    }
});
