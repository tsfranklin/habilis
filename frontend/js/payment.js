// Payment page logic
const API_BASE_URL = 'http://localhost:8080/api';

// Load payment data on page load
document.addEventListener('DOMContentLoaded', () => {
    loadPaymentData();
    setupPaymentForm();
});

function loadPaymentData() {
    // Get payment data from sessionStorage
    const paymentDataStr = sessionStorage.getItem('paymentData');
    const pendingQuizStr = sessionStorage.getItem('pendingQuizCheckout');

    let paymentData = null;

    // Try paymentData first (logged-in user from quiz)
    if (paymentDataStr) {
        paymentData = JSON.parse(paymentDataStr);
    }
    // Fallback to pendingQuizCheckout (user who registered and logged in)
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
    }

    if (!paymentData) {
        alert('No hay datos de pago. Serás redirigido al catálogo.');
        window.location.href = 'catalog.html';
        return;
    }

    // Display order summary
    document.getElementById('summaryProduct').textContent = paymentData.productName;
    document.getElementById('summaryChild').textContent = `${paymentData.childName} (${paymentData.childAge} años)`;
    document.getElementById('summaryProfile').textContent = paymentData.profile;
    document.getElementById('summaryTotal').textContent = `€${paymentData.productPrice.toFixed(2)}`;

    // Store for form submission
    window.currentPaymentData = paymentData;
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

        const orderData = {
            items: [{
                productoId: paymentData.productId,
                cantidad: 1
            }]
        };

        console.log('Creating order:', orderData);

        const response = await fetch(`${API_BASE_URL}/pedidos`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            credentials: 'include',
            body: JSON.stringify(orderData)
        });

        console.log('Response status:', response.status);

        if (!response.ok) {
            let errorMessage = 'Error al procesar el pago';
            const contentType = response.headers.get('content-type');

            if (contentType && contentType.includes('application/json')) {
                try {
                    const errorData = await response.json();
                    errorMessage = errorData.error || errorData.message || errorMessage;
                } catch (e) {
                    errorMessage = `Error ${response.status}: ${response.statusText}`;
                }
            } else {
                errorMessage = `Error ${response.status}: ${response.statusText}`;
            }

            throw new Error(errorMessage);
        }

        const data = await response.json();
        console.log('Order created:', data);

        if (data.success && data.pedido && data.codigoFactura) {
            // Success! Clear session storage
            sessionStorage.removeItem('paymentData');
            sessionStorage.removeItem('pendingQuizCheckout');

            // Show brief success message
            showMessage('paymentSuccess', '¡Pago procesado exitosamente! Redirigiendo...');

            // Redirect to confirmation page
            setTimeout(() => {
                window.location.href = `order-confirmation.html?orderId=${data.pedido.id}&invoiceCode=${data.codigoFactura}`;
            }, 1500);
        } else {
            throw new Error(data.message || 'Error al crear el pedido');
        }

    } catch (error) {
        console.error('Payment error:', error);
        showMessage('paymentError', error.message || 'Error al procesar el pago. Inténtalo de nuevo.');

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
