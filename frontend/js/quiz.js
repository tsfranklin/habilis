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

// Questions (5 preguntas mejoradas - Basadas en Gardner/Piaget)
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
        context: 'Ante un regalo cerrado (caja nueva)',
        options: [
            { letter: 'A', text: 'Pregunta "¿Qué es?" o intenta averiguar cómo abrirlo', profile: 'Lógico' },
            { letter: 'B', text: 'Rompe el papel rápido y la agita', profile: 'Motor' },
            { letter: 'C', text: 'Se fija en los dibujos y colores del papel', profile: 'Artista' }
        ]
    },
    {
        id: 3,
        text: '¿qué hace {childName}?',
        context: 'Si se le rompe un juguete',
        options: [
            { letter: 'A', text: 'Intenta arreglarlo buscando dónde encaja la pieza', profile: 'Lógico' },
            { letter: 'B', text: 'Te pide ayuda y te explica lo que pasó', profile: 'Explorador' },
            { letter: 'C', text: 'Se frustra físicamente o lo tira', profile: 'Motor' },
            { letter: 'D', text: 'Usa las piezas rotas para inventar otra cosa', profile: 'Artista' }
        ]
    },
    {
        id: 4,
        text: '¿dónde pasa más tiempo {childName}?',
        context: 'En el parque',
        options: [
            { letter: 'A', text: 'En el arenero buscando piedras o bichos', profile: 'Explorador' },
            { letter: 'B', text: 'En los columpios, toboganes y trepando', profile: 'Motor' },
            { letter: 'C', text: 'Jugando con otros a imaginar historias (roles)', profile: 'Artista' },
            { letter: 'D', text: 'Observando cómo funciona el balancín', profile: 'Lógico' }
        ]
    },
    {
        id: 5,
        text: '¿qué actividad prefiere {childName}?',
        context: 'Para actividades en casa',
        options: [
            { letter: 'A', text: 'Puzzles y bloques de construcción', profile: 'Lógico' },
            { letter: 'B', text: 'Bailar o juegos de movimiento', profile: 'Motor' },
            { letter: 'C', text: 'Manualidades, tijeras y pegamento', profile: 'Artista' },
            { letter: 'D', text: 'Escuchar cuentos e historias', profile: 'Explorador' }
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

    // Contar frecuencia de cada perfil
    quizState.answers.forEach(answer => {
        profileCounts[answer] = (profileCounts[answer] || 0) + 1;
    });

    // Encontrar el máximo de votos
    let maxCount = 0;
    for (const count of Object.values(profileCounts)) {
        if (count > maxCount) {
            maxCount = count;
        }
    }

    // Obtener todos los perfiles con el máximo de votos
    const winners = [];
    for (const [profile, count] of Object.entries(profileCounts)) {
        if (count === maxCount) {
            winners.push(profile);
        }
    }

    // Sistema de desempate: Lógico > Explorador > Artista > Motor
    const priority = ['Lógico', 'Explorador', 'Artista', 'Motor'];

    if (winners.length === 1) {
        quizState.profile = winners[0];
    } else {
        // Empate: elegir según prioridad
        for (const profile of priority) {
            if (winners.includes(profile)) {
                quizState.profile = profile;
                break;
            }
        }
    }

    console.log('Respuestas:', quizState.answers);
    console.log('Conteo de perfiles:', profileCounts);
    console.log('Perfil ganador:', quizState.profile);
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
        console.log('Buscando producto ID:', kitId, 'para perfil:', quizState.profile);

        const response = await fetch(`${API_BASE_URL}/productos/${kitId}`);

        if (!response.ok) {
            throw new Error('Producto no encontrado');
        }

        const kit = await response.json();
        quizState.selectedKit = kit;

        console.log('Producto encontrado:', kit);

        // Descripciones pedagógicas por perfil
        const profileDescriptions = {
            'Lógico': `🧮 <strong>Inteligencia Lógico-Matemática</strong><br>Hemos detectado que ${quizState.childName} disfruta construyendo, clasificando y entendiendo cómo funcionan las cosas. Este kit potenciará su razonamiento lógico y pensamiento matemático.`,
            'Artista': `🎨 <strong>Inteligencia Visual-Espacial</strong><br>Hemos detectado que ${quizState.childName} tiene una gran creatividad artística y sensibilidad visual. Este kit desarrollará su expresión creativa y habilidades manuales.`,
            'Motor': `🤸 <strong>Inteligencia Cinestésico-Corporal</strong><br>Hemos detectado que ${quizState.childName} aprende mejor a través del movimiento y la acción. Este kit canalizará su energía de forma educativa y estructurada.`,
            'Explorador': `🌿 <strong>Inteligencia Naturalista</strong><br>Hemos detectado que ${quizState.childName} es un explorador natural con gran curiosidad por el entorno. Este kit alimentará su amor por la naturaleza y el descubrimiento.`
        };

        // Renderizar resultado con imagen
        resultCard.innerHTML = `
            <div style="text-align: center; margin-bottom: 1.5rem;">
                <div style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); 
                            border-radius: var(--radius); 
                            padding: 3rem 2rem; 
                            color: white;
                            font-size: 4rem;">
                    ${quizState.profile === 'Lógico' ? '🧮' : quizState.profile === 'Artista' ? '🎨' : quizState.profile === 'Motor' ? '🤸' : '🌿'}
                </div>
            </div>
            <div class="kit-info" style="text-align: center;">
                <h3 class="kit-name" style="font-size: 1.75rem; color: var(--primary); margin-bottom: 1rem;">
                    ${kit.nombre}
                </h3>
                <p class="kit-description" style="color: var(--text-light); margin-bottom: 1.5rem; line-height: 1.8;">
                    ${profileDescriptions[quizState.profile]}
                </p>
                <div style="background: var(--bg-light); padding: 1.5rem; border-radius: var(--radius); margin-bottom: 1.5rem;">
                    <p style="color: var(--text-dark); margin-bottom: 0.5rem;">
                        <strong>📦 Contenido del Mes 1:</strong>
                    </p>
                    <p style="color: var(--text-light); font-size: 0.95rem;">
                        ${kit.descripcion}
                    </p>
                </div>
                <div class="kit-badge" style="display: inline-block; background: var(--accent); color: white; padding: 0.75rem 1.5rem; border-radius: 50px; font-weight: 600; margin-bottom: 1rem;">
                    <i class="fas fa-check-circle"></i> Perfil: ${quizState.profile}${quizState.profile === 'Lógico' ? '-Matemático' : quizState.profile === 'Artista' ? ' Creativo' : quizState.profile === 'Motor' ? ' Activo' : ' Naturalista'}
                </div>
                <div style="font-size: 2rem; font-weight: 700; color: var(--secondary); margin-top: 1rem;">
                    ${kit.precio.toFixed(2)}€ <span style="font-size: 1rem; font-weight: 400; color: var(--text-light);">/mes</span>
                </div>
                <p style="font-size: 0.875rem; color: var(--text-light); margin-top: 0.5rem;">
                    🔒 Cancela cuando quieras • 📦 Envío gratis • ✨ 30 días de garantía
                </p>
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
            // Usuario logueado - proceder con checkout
            const userData = await userResponse.json();
            userId = userData.id;

            console.log('Usuario logueado:', userId);

            // CRÍTICO: Crear pedido en backend
            const orderData = {
                usuarioId: userId,
                items: [{
                    productoId: quizState.selectedKit.id,
                    cantidad: 1
                }]
            };

            console.log('Enviando pedido:', orderData);

            const response = await fetch(`${API_BASE_URL}/pedidos`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                credentials: 'include',
                body: JSON.stringify(orderData)
            });

            console.log('Response status:', response.status);

            // Verificar si la respuesta es exitosa (status 2xx)
            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.error || errorData.message || 'Error en el servidor');
            }

            const data = await response.json();
            console.log('Respuesta del servidor:', data);

            // El backend retorna {success: true, message: "...", pedido: {...}}
            if (data.success && data.pedido) {
                // Pedido creado exitosamente
                alert(`¡Pedido creado exitosamente! 🎉\n\nNúmero de pedido: #${data.pedido.id}\nTotal: €${quizState.selectedKit.precio.toFixed(2)}\n\nRecibirás un email de confirmación en: ${email}`);

                // Limpiar sessionStorage si existe
                sessionStorage.removeItem('pendingQuizCheckout');

                // Redirigir a dashboard
                window.location.href = 'user-dashboard.html';
            } else {
                throw new Error(data.message || 'Error al crear pedido');
            }

        } else {
            // Usuario NO logueado - guardar datos del quiz y redirigir a registro
            console.log('Usuario no logueado, guardando datos del quiz...');

            const quizData = {
                childName: quizState.childName,
                childAge: quizState.childAge,
                profile: quizState.profile,
                productId: quizState.selectedKit.id,
                productName: quizState.selectedKit.nombre,
                productPrice: quizState.selectedKit.precio,
                email: email,
                timestamp: new Date().toISOString()
            };

            // Guardar en sessionStorage para recuperar después del registro
            sessionStorage.setItem('pendingQuizCheckout', JSON.stringify(quizData));
            console.log('Datos guardados en sessionStorage:', quizData);

            // Mostrar mensaje y redirigir a registro
            alert('¡Genial! Para completar tu pedido necesitas crear una cuenta.\n\nTe redirigiremos al registro. Tus datos del quiz están guardados. 😊');

            // Redirigir a registro con parámetro para indicar que viene del quiz
            window.location.href = `register.html?from=quiz&product=${quizState.selectedKit.id}`;
            return;
        }

    } catch (error) {
        console.error('Error al completar pedido:', error);
        console.error('Error detallado:', error.message);
        alert(`Error al procesar el pedido:\n\n${error.message}\n\nPor favor, inténtalo de nuevo.`);
        btnCheckout.disabled = false;
        btnCheckout.innerHTML = '<i class="fas fa-lock"></i> Ir al Pago Seguro';
    }
}

// ========================================
// INITIALIZATION
// ========================================

document.addEventListener('DOMContentLoaded', () => {
    console.log('Quiz Funnel Loaded');

    // Check if user is returning from registration to complete checkout
    const urlParams = new URLSearchParams(window.location.search);
    const resumeCheckout = urlParams.get('resumeCheckout');

    if (resumeCheckout === 'true') {
        const savedQuiz = sessionStorage.getItem('pendingQuizCheckout');
        if (savedQuiz) {
            console.log('Restaurando datos del quiz...');
            const quizData = JSON.parse(savedQuiz);

            // Restore quiz state
            quizState.childName = quizData.childName;
            quizState.childAge = quizData.childAge;
            quizState.profile = quizData.profile;
            quizState.selectedKit = {
                id: quizData.productId,
                nombre: quizData.productName,
                precio: quizData.productPrice
            };

            // Pre-fill email
            const emailInput = document.getElementById('parentEmail');
            if (emailInput && quizData.email) {
                emailInput.value = quizData.email;
            }

            // Navigate directly to step 4 (checkout)
            console.log('Mostrando paso 4 (checkout)');
            goToStep(4);
            showCheckoutSummary();

            // Show message to user
            setTimeout(() => {
                alert('👋 ¡Bienvenido!\n\nAhora puedes completar tu pedido.');
            }, 500);
        } else {
            console.warn('No se encontraron datos del quiz en sessionStorage');
            updateProgressBar(1);
        }
    } else {
        updateProgressBar(1);
    }
});
