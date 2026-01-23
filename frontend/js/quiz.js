// ========================================
// QUIZ FUNNEL - JAVASCRIPT
// ========================================

const API_BASE_URL = 'http://localhost:8080/api';

// State
const quizState = {
    currentStep: 1,
    childName: '',
    childAge: '',
    answers: [],
    profile: '',
    selectedKit: null,
    currentQuestion: 0
};

// Questions (5 preguntas)
const questions = [
    {
        id: 1,
        text: '¿qué hace {childName}?',
        context: 'Cuando hay juguetes desordenados',
        options: [
            { letter: 'A', text: 'Los agrupa por colores o tamaños', profile: 'Lógico' },
            { letter: 'B', text: 'Se pone a dibujar o pintar', profile: 'Artista' },
            { letter: 'C', text: 'Salta, corre o lanza objetos', profile: 'Motor' },
            { letter: 'D', text: 'Mira por la ventana o busca bichitos', profile: 'Explorador' }
        ]
    },
    {
        id: 2,
        text: '¿qué hace {childName}?',
        context: 'Al recibir un regalo cerrado',
        options: [
            { letter: 'A', text: 'Pregunta cómo funciona antes de abrirlo', profile: 'Lógico' },
            { letter: 'B', text: 'Rompe el papel rápidamente', profile: 'Motor' },
            { letter: 'C', text: 'Mira los dibujos del papel con atención', profile: 'Artista' }
        ]
    },
    {
        id: 3,
        text: '¿qué hace {childName}?',
        context: 'Si un juguete se rompe',
        options: [
            { letter: 'A', text: 'Intenta arreglarlo o descubrir cómo funciona', profile: 'Lógico' },
            { letter: 'B', text: 'Se frustra y lo lanza', profile: 'Motor' },
            { letter: 'C', text: 'Inventa otro uso para él', profile: 'Artista' }
        ]
    },
    {
        id: 4,
        text: '¿qué prefiere {childName}?',
        context: 'En el parque',
        options: [
            { letter: 'A', text: 'Observar cómo funcionan los mecanismos', profile: 'Lógico' },
            { letter: 'B', text: 'Columpios, trepar, correr', profile: 'Motor' },
            { letter: 'C', text: 'Juegos de roles o interacciones sociales', profile: 'Explorador' }
        ]
    },
    {
        id: 5,
        text: '¿qué prefiere {childName}?',
        context: 'Para actividades en casa',
        options: [
            { letter: 'A', text: 'Puzzles, bloques de construcción', profile: 'Lógico' },
            { letter: 'B', text: 'Bailar, saltar, actividades físicas', profile: 'Motor' },
            { letter: 'C', text: 'Manualidades, pintar, tijeras', profile: 'Artista' }
        ]
    }
];

// Kit Mapping (IDs de productos reales en la BD)
// Basado en: Exploradores (1-3), Inventores (4-6), Creadores (7-9)
const kitMapping = {
    'Lógico': 4,      // Kit Inventor: Robot Solar (para perfil lógico/ingeniero)
    'Artista': 1,     // Kit Explorador: Mi Primer Jardín (para perfil artista/creativo) 
    'Motor': 7,       // Kit Creador: Brazo Robótico (para perfil motor/activo)
    'Explorador': 2   // Kit Explorador: Animales de Papel (para perfil explorador)
};

// ========================================
// NAVIGATION
// ========================================

function goToStep(stepNumber) {
    // Validar antes de avanzar
    if (stepNumber === 2 && quizState.currentStep === 1) {
        const name = document.getElementById('childName').value.trim();
        const age = document.getElementById('childAge').value;

        if (!name || !age) {
            alert('Por favor, completa el nombre y la edad del niño');
            return;
        }

        quizState.childName = name;
        quizState.childAge = age;
        quizState.currentQuestion = 0;
        quizState.answers = [];
    }

    // Ocultar todos los pasos
    document.querySelectorAll('.quiz-step').forEach(step => {
        step.classList.remove('active');
    });

    // Mostrar paso seleccionado
    document.getElementById(`step${stepNumber}`).classList.add('active');

    // Actualizar barra de progreso
    updateProgressBar(stepNumber);

    // Actualizar estado
    quizState.currentStep = stepNumber;

    // Acciones específicas por paso
    if (stepNumber === 2) {
        showQuestion(0);
    } else if (stepNumber === 3) {
        calculateProfile();
        showResult();
    } else if (stepNumber === 4) {
        showCheckoutSummary();
    }

    // Scroll to top
    window.scrollTo(0, 0);
}

function updateProgressBar(currentStep) {
    document.querySelectorAll('.progress-step').forEach((step, index) => {
        const stepNum = index + 1;

        step.classList.remove('active', 'completed');

        if (stepNum === currentStep) {
            step.classList.add('active');
        } else if (stepNum < currentStep) {
            step.classList.add('completed');
        }
    });
}

// ========================================
// STEP 2: QUIZ LOGIC
// ========================================

function showQuestion(questionIndex) {
    const question = questions[questionIndex];
    const questionText = `${question.context}, ${question.text}`.replace('{childName}', quizState.childName);

    document.getElementById('questionNumber').textContent = `Pregunta ${questionIndex + 1} de ${questions.length}`;
    document.getElementById('questionText').textContent = questionText;

    const optionsHTML = question.options.map(option => `
        <div class="quiz-option" onclick="selectAnswer('${option.profile}')">
            <div class="option-letter">${option.letter}</div>
            <div class="option-text">${option.text}</div>
        </div>
    `).join('');

    document.getElementById('optionsContainer').innerHTML = optionsHTML;
}

function selectAnswer(profile) {
    quizState.answers.push(profile);

    if (quizState.currentQuestion < questions.length - 1) {
        quizState.currentQuestion++;
        setTimeout(() => {
            showQuestion(quizState.currentQuestion);
        }, 300);
    } else {
        // Test completado, avanzar a resultado
        setTimeout(() => {
            goToStep(3);
        }, 500);
    }
}

function calculateProfile() {
    const profileCounts = {};

    quizState.answers.forEach(answer => {
        profileCounts[answer] = (profileCounts[answer] || 0) + 1;
    });

    // Encontrar perfil ganador
    let maxCount = 0;
    let winner = 'Lógico'; // Default

    for (const [profile, count] of Object.entries(profileCounts)) {
        if (count > maxCount) {
            maxCount = count;
            winner = profile;
        }
    }

    quizState.profile = winner;
}

// ========================================
// STEP 3: RESULTADO (BACKEND INTEGRATION)
// ========================================

async function showResult() {
    document.getElementById('childNameResult').textContent = quizState.childName;

    const resultCard = document.getElementById('resultCard');
    resultCard.innerHTML = '<div class="loading"><i class="fas fa-spinner fa-spin"></i> Cargando recomendación...</div>';

    try {
        // CRÍTICO: Fetch del producto desde backend
        const kitId = kitMapping[quizState.profile] || 1;
        const response = await fetch(`${API_BASE_URL}/productos/${kitId}`);

        if (!response.ok) {
            throw new Error('Producto no encontrado');
        }

        const kit = await response.json();
        quizState.selectedKit = kit;

        // Mostrar resultado
        const profileDescriptions = {
            'Lógico': `Hemos detectado que ${quizState.childName} disfruta construyendo y entendiendo cómo funcionan las cosas. Este kit potenciará su lógica y creatividad.`,
            'Artista': `Hemos detectado que ${quizState.childName} tiene una gran creatividad artística. Este kit desarrollará su expresión y habilidades manuales.`,
            'Motor': `Hemos detectado que ${quizState.childName} aprende mejor a través del movimiento. Este kit canalizará su energía de forma educativa.`,
            'Explorador': `Hemos detectado que ${quizState.childName} es un explorador natural. Este kit alimentará su curiosidad por el mundo.`
        };

        resultCard.innerHTML = `
            <div class="kit-image">${kit.imagenUrl || '📦'}</div>
            <div class="kit-info">
                <h3 class="kit-name">${kit.nombre}</h3>
                <p class="kit-description">${profileDescriptions[quizState.profile]}</p>
                <div class="kit-badge">
                    <i class="fas fa-check-circle"></i> Potencia: ${quizState.profile}${quizState.profile === 'Lógico' ? '-Matemática' : ''}
                </div>
            </div>
        `;

        document.getElementById('btnContinueCheckout').style.display = 'block';

    } catch (error) {
        console.error('Error cargando producto:', error);
        console.error('Perfil actual:', quizState.profile);
        console.error('Kit ID:', kitMapping[quizState.profile]);

        resultCard.innerHTML = `
            <div class="alert alert-error">
                <p>Error al cargar el kit recomendado. Por favor, inténtalo de nuevo.</p>
                <p style="font-size: 0.875rem; color: var(--text-light); margin-top: 0.5rem;">
                    Error técnico: ${error.message}
                </p>
                <button id="btnRetry" class="btn btn-primary" style="margin-top: 1rem;">
                    <i class="fas fa-redo"></i> Reintentar
                </button>
            </div>
        `;

        // Añadir event listener al botón después de crearlo
        document.getElementById('btnRetry').addEventListener('click', function () {
            showResult();
        });
    }
}

// ========================================
// STEP 4: CHECKOUT (BACKEND INTEGRATION)
// ========================================

function showCheckoutSummary() {
    if (!quizState.selectedKit) return;

    document.getElementById('checkoutProduct').textContent = quizState.selectedKit.nombre;
    document.getElementById('checkoutChild').textContent = quizState.childName;
    document.getElementById('checkoutTotal').textContent = `€${quizState.selectedKit.precio.toFixed(2)}`;
}

async function completeOrder() {
    const email = document.getElementById('parentEmail').value.trim();

    if (!email) {
        alert('Por favor, ingresa tu correo electrónico');
        return;
    }

    const btnCheckout = document.getElementById('btnCheckout');
    btnCheckout.disabled = true;
    btnCheckout.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Procesando...';

    try {
        // Verificar sesión
        const userResponse = await fetch(`${API_BASE_URL}/auth/me`, { credentials: 'include' });

        let userId;
        if (userResponse.ok) {
            const userData = await userResponse.json();
            userId = userData.id;
        } else {
            // Usuario no logueado - podría crear cuenta automática o mostrar error
            alert('Debes iniciar sesión para completar la compra. Redirigiendo...');
            window.location.href = `login.html?redirect=quiz.html`;
            return;
        }

        // CRÍTICO: Crear pedido en backend
        const orderData = {
            usuarioId: userId,
            items: [{
                productoId: quizState.selectedKit.id,
                cantidad: 1
            }]
        };

        const response = await fetch(`${API_BASE_URL}/pedidos`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            credentials: 'include',
            body: JSON.stringify(orderData)
        });

        const data = await response.json();

        if (data.success) {
            // Pedido creado exitosamente
            alert(`¡Pedido creado exitosamente! 🎉\n\nNúmero de pedido: #${data.pedidoId || 'XXXX'}\nTotal: €${quizState.selectedKit.precio.toFixed(2)}\n\nRecibirás un email de confirmación en: ${email}`);

            // Redirigir a dashboard
            window.location.href = 'user-dashboard.html';
        } else {
            throw new Error(data.message || 'Error al crear pedido');
        }

    } catch (error) {
        console.error('Error al completar pedido:', error);
        alert('Error al procesar el pedido. Por favor, inténtalo de nuevo.');
        btnCheckout.disabled = false;
        btnCheckout.innerHTML = '<i class="fas fa-lock"></i> Ir al Pago Seguro';
    }
}

// ========================================
// INITIALIZATION
// ========================================

document.addEventListener('DOMContentLoaded', () => {
    console.log('Quiz Funnel Loaded');
    updateProgressBar(1);
});
