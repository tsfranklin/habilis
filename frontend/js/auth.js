// ========================================
// CONFIGURACIÓN Y UTILIDADES
// ========================================

const API_BASE_URL = '/api';

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
                // Login successful
                showMessage('loginSuccess', 'Inicio de sesión exitoso', 'success');

                // Check if user came from quiz checkout
                const pendingQuiz = localStorage.getItem('pendingQuizCheckout');

                if (pendingQuiz) {
                    // User has pending quiz data - redirect back to complete checkout
                    console.log('Datos del quiz detectados, redirigiendo a completar pedido...');
                    setTimeout(() => {
                        window.location.href = 'quiz.html?resumeCheckout=true';
                    }, 1000);
                } else {
                    // Normal login - redirect to dashboard
                    setTimeout(() => {
                        window.location.href = data.tipoUsuario === 'ADMIN' ? 'admin-dashboard.html' : 'user-dashboard.html';
                    }, 1000);
                }
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

    console.log('Verificando 2FA para usuario:', userId, 'código:', code);

    showLoader(submitBtn);

    try {
        const response = await fetch(`${API_BASE_URL}/auth/verificar-2fa?usuarioId=${userId}&codigo=${code}`, {
            method: 'POST',
            credentials: 'include'
        });

        const data = await response.json();

        if (data.success) {
            showMessage('twoFactorSuccess', '¡Código correcto! Redirigiendo...', 'success');

            // Check if user has pending quiz data
            const pendingQuiz = localStorage.getItem('pendingQuizCheckout');

            if (pendingQuiz) {
                // User has pending quiz data - redirect back to complete checkout
                console.log('Datos del quiz detectados (post-2FA), redirigiendo a completar pedido...');
                setTimeout(() => {
                    window.location.href = 'quiz.html?resumeCheckout=true';
                }, 1000);
            } else {
                // Normal redirect to dashboard
                setTimeout(() => {
                    window.location.href = data.tipoUsuario === 'ADMIN' ? 'admin-dashboard.html' : 'user-dashboard.html';
                }, 1000);
            }
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

            // Always redirect to confirmation page (requirement from tutor)
            // If user came from quiz, the confirm-email page will handle the redirect back
            setTimeout(() => {
                // Pass quiz parameters to confirmation page
                const urlParams = new URLSearchParams(window.location.search);
                const fromQuiz = urlParams.get('from');
                const email = urlParams.get('email');

                if (fromQuiz === 'quiz' && email) {
                    window.location.href = `confirm-email.html?from=quiz&email=${encodeURIComponent(email)}`;
                } else {
                    window.location.href = 'confirm-email.html';
                }
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

// ========================================
// RECUPERACIÓN DE CONTRASEÑA
// ========================================

document.getElementById('forgotPasswordForm')?.addEventListener('submit', async (e) => {
    e.preventDefault();

    const form = e.target;
    const submitBtn = form.querySelector('button[type="submit"]');
    const email = document.getElementById('forgotEmail').value;

    showLoader(submitBtn);

    try {
        const response = await fetch(`${API_BASE_URL}/auth/recuperar-password?correoElectronico=${encodeURIComponent(email)}`, {
            method: 'POST',
            credentials: 'include'
        });

        const data = await response.json();

        if (data.success) {
            document.getElementById('forgotError').style.display = 'none';
            document.getElementById('forgotSuccess').textContent = data.message ||
                'Si el correo existe, recibirás instrucciones para restablecer tu contraseña';
            document.getElementById('forgotSuccess').style.display = 'block';

            // Clear form
            form.reset();

            console.log('✅ Email de recuperación solicitado');
        } else {
            showMessage('forgotError', data.message || 'Error al solicitar recuperación');
        }
    } catch (error) {
        console.error('Forgot password error:', error);
        showMessage('forgotError', 'Error de conexión con el servidor');
    } finally {
        hideLoader(submitBtn);
    }
});

// ========================================
// RESTABLECER CONTRASEÑA (con token)
// ========================================

document.getElementById('resetPasswordForm')?.addEventListener('submit', async (e) => {
    e.preventDefault();

    const form = e.target;
    const submitBtn = form.querySelector('button[type="submit"]');
    const newPassword = document.getElementById('newPassword').value;
    const confirmPassword = document.getElementById('confirmPassword').value;

    // Obtener token de la URL
    const urlParams = new URLSearchParams(window.location.search);
    const token = urlParams.get('token');

    if (!token) {
        showMessage('resetError', 'Token de recuperación inválido o expirado');
        return;
    }

    // Validar que las contraseñas coincidan
    if (newPassword !== confirmPassword) {
        showMessage('resetError', 'Las contraseñas no coinciden');
        return;
    }

    showLoader(submitBtn);

    try {
        const response = await fetch(`${API_BASE_URL}/auth/restablecer-password?token=${encodeURIComponent(token)}&nuevaContrasena=${encodeURIComponent(newPassword)}`, {
            method: 'POST',
            credentials: 'include'
        });

        const data = await response.json();

        if (data.success) {
            document.getElementById('resetError').style.display = 'none';
            document.getElementById('resetSuccess').textContent = '¡Contraseña restablecida exitosamente! Redirigiendo al login...';
            document.getElementById('resetSuccess').style.display = 'block';

            console.log('✅ Contraseña restablecida');

            // Redirect to login after 2 seconds
            setTimeout(() => {
                window.location.href = 'login.html';
            }, 2000);
        } else {
            showMessage('resetError', data.message || 'Error al restablecer contraseña');
        }
    } catch (error) {
        console.error('Reset password error:', error);
        showMessage('resetError', 'Error de conexión con el servidor');
    } finally {
        hideLoader(submitBtn);
    }
});
