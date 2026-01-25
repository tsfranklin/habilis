-- ========================================
-- HÁBILIS - Data Seeding Script
-- Sistema de Suscripción Mensual
-- ========================================

-- 1. Limpiar tablas existentes y reiniciar secuencias
TRUNCATE TABLE detalle_pedido, pedidos, productos, categorias RESTART IDENTITY CASCADE;

-- 2. Crear Categorías Pedagógicas (Basadas en Piaget/Gardner)
INSERT INTO categorias (nombre, descripcion) VALUES 
('Exploradores (3-4 años)', 'Etapa sensorial y motricidad fina - Desarrollo de los sentidos y coordinación'),
('Inventores (5-6 años)', 'Etapa pre-lógica y descubrimiento - Pensamiento simbólico y experimentación'),
('Creadores (7+ años)', 'Etapa de operaciones concretas - Razonamiento lógico y construcción');

-- 3. INSERTAR PRODUCTOS "HOOK" (Mes 1 de Suscripción)
-- CRÍTICO: IDs forzados (1, 2, 4, 7) para coincidir con quiz.js
-- Precio único: 24.90€ para todos los perfiles

INSERT INTO productos (id, nombre, descripcion, precio, stock, categoria_id) OVERRIDING SYSTEM VALUE VALUES
-- ID 1: Perfil ARTISTA (Inteligencia Visual/Espacial - Gardner)
(1, 'Suscripción: El Mundo del Color', 
'Mes 1: Kit de discriminación visual y arte. Incluye actividades de clasificación cromática, mezcla de pinturas naturales y el juego de "Familias de Colores". Desarrollo de la inteligencia visual-espacial mediante la exploración sensorial del color. Inicio del plan anual creativo.', 
24.90, 100, 1),

-- ID 2: Perfil EXPLORADOR (Inteligencia Naturalista - Gardner)
(2, 'Suscripción: Pequeño Naturalista', 
'Mes 1: Kit de descubrimiento del entorno. Incluye lupa de madera, guía de bichitos del parque, macetas biodegradables y semillas. Actividades de observación sensorial y clasificación de elementos naturales. Desarrollo de la inteligencia naturalista. Inicio del plan anual de naturaleza.', 
24.90, 100, 1),

-- ID 4: Perfil LÓGICO (Inteligencia Lógico-Matemática - Gardner)
(4, 'Suscripción: Mente Matemática', 
'Mes 1: Kit de lógica y patrones. Incluye el juego "Conteo Marino", fichas de secuenciación numérica, bloques de madera para causa-efecto y ábaco de colores. Desarrollo del pensamiento lógico-matemático mediante manipulación concreta. Inicio del plan anual lógico.', 
24.90, 100, 2),

-- ID 7: Perfil MOTOR (Inteligencia Cinestésico-Corporal - Gardner)
(7, 'Suscripción: Movimiento Libre', 
'Mes 1: Kit de psicomotricidad. Incluye circuito de texturas para pies, pelotas sensoriales de diferentes tamaños, tabla de equilibrio y retos de coordinación. Desarrollo de la inteligencia cinestésico-corporal. Inicio del plan anual de movimiento.', 
24.90, 100, 3);

-- 4. Ajustar secuencia para futuros inserts
SELECT setval('productos_id_seq', (SELECT MAX(id) FROM productos));

-- 5. Verificación (Comentado - solo para debug)
-- SELECT id, nombre, precio, categoria_id FROM productos ORDER BY id;
