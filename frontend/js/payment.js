// Payment page logic
const API_BASE_URL = '/api';

// Load payment data on page load
document.addEventListener('DOMContentLoaded', () => {
    loadPaymentData();
    setupPaymentForm();
});

async function loadPaymentData() {
    // PRIMERO: Verificar que el usuario está logueado
    try {
        const sessionCheck = await fetch(`${API_BASE_URL}/auth/me`, {
            credentials: 'include'
        });

        if (!sessionCheck.ok) {
            alert('⚠️ No has iniciado sesión. Serás redirigido al login.');
            window.location.href = 'login.html';
            return;
        }

        console.log('✅ Usuario autenticado correctamente');
    } catch (error) {
        console.error('❌ Error verificando sesión:', error);
        alert('⚠️ Error de conexión. Por favor, inicia sesión nuevamente.');
        window.location.href = 'login.html';
        return;
    }

    // SEGUNDO: Obtener datos de pago de sessionStorage o del carrito
    const paymentDataStr = sessionStorage.getItem('paymentData');
    const pendingQuizStr = sessionStorage.getItem('pendingQuizCheckout');
    const cartStr = localStorage.getItem('cart');

    let paymentData = null;

    // Prioridad 1: paymentData (usuario logueado desde quiz)
    if (paymentDataStr) {
        paymentData = JSON.parse(paymentDataStr);
        console.log('✅ Datos cargados desde paymentData (quiz)');
    }
    // Prioridad 2: pendingQuizCheckout (usuario que se registró y logueó)
    else if (pendingQuizStr) {
        const pendingQuiz = JSON.parse(pendingQuizStr);
        paymentData = {
            productId: pendingQuiz.productId,
            productName: pendingQuiz.productName,
            productPrice: pendingQuiz.productPrice,
            childName: pendingQuiz.childName,
            childAge: pendingQuiz.childAge,
            profile: pendingQuiz.profile,
            userEmail: pendingQuiz.email
        };
        console.log('✅ Datos cargados desde pendingQuizCheckout');
    }
    // Prioridad 3: Carrito (usuario agregó productos desde catálogo)
    else if (cartStr) {
        const cart = JSON.parse(cartStr);

        if (cart.length === 0) {
            alert('⚠️ Tu carrito está vacío.');
            window.location.href = 'catalog.html';
            return;
        }

        // Calcular total del carrito
        const total = cart.reduce((sum, item) => sum + (item.price * item.quantity), 0);

        // Crear datos de pago desde el carrito
        paymentData = {
            productId: cart[0].id, // Primer producto (o podríamos crear un pedido múltiple)
            productName: cart.length === 1
                ? cart[0].name
                : `${cart.length} productos`,
            productPrice: total,
            childName: 'Cliente', // No tenemos datos del niño desde el carrito
            childAge: '', // No tenemos edad desde el carrito
            profile: '', // No tenemos perfil desde el carrito
            fromCart: true, // Flag para identificar que viene del carrito
            cartItems: cart // Guardar todos los items del carrito
        };
        console.log('✅ Datos cargados desde carrito:', paymentData);
    }

    if (!paymentData) {
        alert('⚠️ No hay datos de pago. Por favor, agrega productos al carrito o completa el quiz.');
        window.location.href = 'catalog.html';
        return;
    }

    // Display order summary
    document.getElementById('summaryProduct').textContent = paymentData.productName;

    // Mostrar información del niño solo si existe (desde quiz)
    const childInfo = document.getElementById('summaryChild');
    const profileInfo = document.getElementById('summaryProfile');

    if (paymentData.fromCart) {
        // Desde carrito - ocultar campos de niño y perfil
        childInfo.textContent = '-';
        profileInfo.textContent = '-';
    } else {
        // Desde quiz - mostrar datos del niño
        childInfo.textContent = `${paymentData.childName} (${paymentData.childAge} años)`;
        profileInfo.textContent = paymentData.profile;
    }

    document.getElementById('summaryTotal').textContent = `€${paymentData.productPrice.toFixed(2)}`;

    // Store for form submission
    window.currentPaymentData = paymentData;

    console.log('✅ Datos de pago cargados correctamente:', paymentData);
}

function setupPaymentForm() {
    const form = document.getElementById('paymentForm');
    const cardNumber = document.getElementById('cardNumber');
    const cardExpiry = document.getElementById('cardExpiry');
    const cardCVV = document.getElementById('cardCVV');

    // Format card number as user types
    cardNumber.addEventListener('input', (e) => {
        let value = e.target.value.replace(/\s/g, '').replace(/\D/g, '');
        let formattedValue = value.match(/.{1,4}/g)?.join(' ') || value;
        e.target.value = formattedValue;
    });

    // Format expiry date
    cardExpiry.addEventListener('input', (e) => {
        let value = e.target.value.replace(/\D/g, '');
        if (value.length >= 2) {
            value = value.substring(0, 2) + '/' + value.substring(2, 4);
        }
        e.target.value = value;
    });

    // Only numbers for CVV
    cardCVV.addEventListener('input', (e) => {
        e.target.value = e.target.value.replace(/\D/g, '');
    });

    // Handle form submission
    form.addEventListener('submit', handlePaymentSubmit);
}

async function handlePaymentSubmit(e) {
    e.preventDefault();

    const submitBtn = e.target.querySelector('button[type="submit"]');
    const btnText = submitBtn.querySelector('.btn-text');
    const btnLoader = submitBtn.querySelector('.btn-loader');

    // Validate form
    const cardNumber = document.getElementById('cardNumber').value.replace(/\s/g, '');
    const cardExpiry = document.getElementById('cardExpiry').value;
    const cardCVV = document.getElementById('cardCVV').value;
    const cardName = document.getElementById('cardName').value;

    if (cardNumber.length !== 16) {
        showMessage('paymentError', 'El número de tarjeta debe tener 16 dígitos');
        return;
    }

    if (!/^\d{2}\/\d{2}$/.test(cardExpiry)) {
        showMessage('paymentError', 'La fecha de vencimiento debe tener formato MM/AA');
        return;
    }

    if (cardCVV.length !== 3) {
        showMessage('paymentError', 'El CVV debe tener 3 dígitos');
        return;
    }

    if (!cardName.trim()) {
        showMessage('paymentError', 'Debes ingresar el nombre del titular');
        return;
    }

    // Show loading state
    btnText.style.display = 'none';
    btnLoader.style.display = 'inline-block';
    submitBtn.disabled = true;

    try {
        // Simulate payment processing delay
        await new Promise(resolve => setTimeout(resolve, 1500));

        // Create order in backend
        const paymentData = window.currentPaymentData;

        let orderData;

        if (paymentData.fromCart && paymentData.cartItems) {
            // Pedido desde carrito - múltiples items
            orderData = {
                items: paymentData.cartItems.map(item => ({
                    productoId: item.id,
                    cantidad: item.quantity
                }))
            };
            console.log('🛒 Creando pedido desde carrito con', paymentData.cartItems.length, 'items');
        } else {
            // Pedido desde quiz - un solo item
            orderData = {
                items: [{
                    productoId: paymentData.productId,
                    cantidad: 1
                }]
            };
            console.log('📝 Creando pedido desde quiz');
        }

        console.log('🔍 Creating order:', orderData);
        console.log('🔍 Payment data:', paymentData);

        const response = await fetch(`${API_BASE_URL}/pedidos`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            credentials: 'include', // IMPORTANTE: Enviar cookies de sesión
            body: JSON.stringify(orderData)
        });

        console.log('🔍 Response status:', response.status);
        console.log('🔍 Response headers:', [...response.headers.entries()]);

        if (!response.ok) {
            let errorMessage = 'Error al procesar el pago';
            const contentType = response.headers.get('content-type');

            if (contentType && contentType.includes('application/json')) {
                try {
                    const errorData = await response.json();
                    errorMessage = errorData.error || errorData.message || errorMessage;
                    console.error('❌ Error del servidor:', errorData);
                } catch (e) {
                    errorMessage = `Error ${response.status}: ${response.statusText}`;
                    console.error('❌ Error parseando respuesta:', e);
                }
            } else {
                const textResponse = await response.text();
                console.error('❌ Respuesta no JSON:', textResponse);
                errorMessage = `Error ${response.status}: ${response.statusText}`;
            }

            throw new Error(errorMessage);
        }

        const data = await response.json();
        console.log('✅ Order created:', data);

        if (data.success && data.pedidoId && data.codigoFactura) {
            // Success! Clear session storage
            sessionStorage.removeItem('paymentData');
            sessionStorage.removeItem('pendingQuizCheckout');

            // Si vino del carrito, limpiar el carrito
            if (paymentData.fromCart) {
                localStorage.removeItem('cart');
                console.log('🛒 Carrito limpiado');
            }

            // Show brief success message
            showMessage('paymentSuccess', '¡Pago procesado exitosamente! Redirigiendo...');

            // Redirect to confirmation page
            setTimeout(() => {
                window.location.href = `order-confirmation.html?orderId=${data.pedidoId}&invoiceCode=${data.codigoFactura}`;
            }, 1500);
        } else {
            throw new Error(data.message || 'Error al crear el pedido');
        }

    } catch (error) {
        console.error('❌ Payment error:', error);

        // Mostrar mensaje de error más específico
        let userMessage = error.message || 'Error al procesar el pago. Inténtalo de nuevo.';

        // Si es error 403, dar instrucciones específicas
        if (error.message.includes('403') || error.message.includes('Forbidden')) {
            userMessage = 'Error de autenticación. Por favor, cierra sesión y vuelve a iniciar sesión.';
        }

        showMessage('paymentError', userMessage);

        // Reset button
        btnText.style.display = 'inline-block';
        btnLoader.style.display = 'none';
        submitBtn.disabled = false;
    }
}

function showMessage(elementId, message, type = 'error') {
    const errorEl = document.getElementById('paymentError');
    const successEl = document.getElementById('paymentSuccess');

    // Hide both first
    errorEl.style.display = 'none';
    successEl.style.display = 'none';

    if (elementId === 'paymentError') {
        errorEl.textContent = message;
        errorEl.style.display = 'block';
    } else {
        successEl.textContent = message;
        successEl.style.display = 'block';
    }
}
