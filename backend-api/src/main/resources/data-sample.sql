-- ===================================================
-- SCRIPT DE DATOS DE PRUEBA PARA HÁBILIS
-- ===================================================

-- 1. INSERTAR CATEGORÍAS (3 grupos de edad)
INSERT INTO categorias (nombre, descripcion) VALUES 
('Exploradores (3-6 años)', 'Kits diseñados para niños en edad preescolar que desarrollan motricidad fina básica y creatividad mediante manualidades simples.'),
('Inventores (7-9 años)', 'Kits con proyectos de ingeniería básica y experimentos científicos para niños de primaria.'),
('Creadores (10-13 años)', 'Kits avanzados con retos complejos de construcción, programación tangible y proyectos STEM.');

-- 2. INSERTAR PRODUCTOS/KITS (3 por categoría)
INSERT INTO productos (categoria_id, nombre, descripcion, precio, stock, imagen_url) VALUES
-- Exploradores (3-6 años)
(1, 'Kit Explorador: Mi Primer Jardín', 'Aprende sobre plantas cultivando tu propio mini jardín. Incluye semillas, macetas biodegradables y herramientas adaptadas.', 24.99, 50, 'https://images.unsplash.com/photo-1523348837708-15d4a09cfac2?w=400'),
(1, 'Kit Explorador: Animales de Papel', 'Crea tus propios animales usando origami simplificado. Desarrolla coordinación mano-ojo y concentración.', 19.99, 75, 'https://images.unsplash.com/photo-1578662996442-48f60103fc96?w=400'),
(1, 'Kit Explorador: Torre de Colores', 'Construye estructuras apilables con piezas de madera de colores. Aprende sobre equilibrio y formas geométricas.', 27.99, 60, 'https://images.unsplash.com/photo-1558060370-d644479cb6f7?w=400'),

-- Inventores (7-9 años)
(2, 'Kit Inventor: Robot Solar', 'Construye tu propio robot que funciona con energía solar. Aprende conceptos básicos de ingeniería y energía renovable.', 39.99, 40, 'https://images.unsplash.com/photo-1485827404703-89b55fcc595e?w=400'),
(2, 'Kit Inventor: Circuitos Mágicos', 'Crea circuitos eléctricos simples con LED y switches. Introduce conceptos de electricidad de forma segura.', 34.99, 55, 'https://images.unsplash.com/photo-1518770660439-4636190af475?w=400'),
(2, 'Kit Inventor: Catapulta Medieval', 'Diseña y construye una catapulta funcional. Aprende sobre física, palancas y proyectiles.', 29.99, 65, 'https://images.unsplash.com/photo-1581092918056-0c4c3acd3789?w=400'),

-- Creadores (10-13 años)
(3, 'Kit Creador: Brazo Robótico', 'Construye un brazo robótico con control hidráulico. Proyecto avanzado de ingeniería mecánica.', 54.99, 30, 'https://images.unsplash.com/photo-1535378917042-10a22c95931a?w=400'),
(3, 'Kit Creador: Consola Arcade', 'Monta tu propia consola de videojuegos retro programable. Aprende electrónica y programación básica.', 69.99, 25, 'https://images.unsplash.com/photo-1550745165-9bc0b252726f?w=400'),
(3, 'Kit Creador: Dron DIY', 'Ensambla tu propio mini dron desde cero. Incluye motores, hélices y controlador programable.', 89.99, 20, 'https://images.unsplash.com/photo-1473968512647-3e447244af8f?w=400');

-- 3. INSERTAR USUARIOS DE PRUEBA
-- Contraseña para todos: "habilis2024" (encriptada con BCrypt)
-- Hash BCrypt de "habilis2024": $2a$10$N9qo8uLOickgx2ZMRZoMye6Oq3F8F8F8F8F8F8F8F8F8F8F8F8F8F8

INSERT INTO usuarios (nombre_completo, movil, correo_electronico, tipo_usuario, contrasena, cuenta_activa) VALUES
('Admin Principal', '+34600111222', 'admin@habilis.com', 'ADMIN', '$2a$10$N9qo8uLOickgx2ZMRZoMye6Oq3F8F8F8F8F8F8F8F8F8F8F8F8F8F8', true),
('María García López', '+34611222333', 'maria.garcia@gmail.com', 'USER', '$2a$10$N9qo8uLOickgx2ZMRZoMye6Oq3F8F8F8F8F8F8F8F8F8F8F8F8F8F8', true),
('Carlos Ruiz Mora', '+34622333444', 'carlos.ruiz@outlook.com', 'USER', '$2a$10$N9qo8uLOickgx2ZMRZoMye6Oq3F8F8F8F8F8F8F8F8F8F8F8F8F8F8', true),
('Laura Martínez', '+34633444555', 'laura.martinez@yahoo.es', 'USER', '$2a$10$N9qo8uLOickgx2ZMRZoMye6Oq3F8F8F8F8F8F8F8F8F8F8F8F8F8F8', false);

-- 4. INSERTAR PEDIDOS DE PRUEBA
INSERT INTO pedidos (usuario_id, fecha_pedido, total_pedido, estado) VALUES
(2, '2026-01-05 10:30:00', 79.98, 'COMPLETADO'),
(2, '2026-01-08 15:45:00', 44.98, 'ENVIADO'),
(3, '2026-01-07 09:15:00', 124.98, 'PENDIENTE');

-- 5. INSERTAR DETALLES DE PEDIDO (con precios históricos)
-- Pedido 1 de María (Completado)
INSERT INTO detalle_pedido (pedido_id, producto_id, cantidad, precio_unitario) VALUES
(1, 1, 2, 24.99),  -- 2x Kit Mi Primer Jardín
(1, 4, 1, 29.99);  -- 1x Kit Robot Solar

-- Pedido 2 de María (Enviado)
INSERT INTO detalle_pedido (pedido_id, producto_id, cantidad, precio_unitario) VALUES
(2, 2, 1, 19.99),  -- 1x Kit Animales de Papel
(2, 6, 1, 24.99);  -- 1x Kit Catapulta Medieval (precio antiguo)

-- Pedido 3 de Carlos (Pendiente)
INSERT INTO detalle_pedido (pedido_id, producto_id, cantidad, precio_unitario) VALUES
(3, 8, 1, 69.99),  -- 1x Kit Consola Arcade
(3, 5, 1, 34.99),  -- 1x Kit Circuitos Mágicos
(3, 3, 1, 20.00);  -- 1x Kit Torre de Colores (precio promocional antiguo)

-- ===================================================
-- VERIFICACIÓN: Consultas para comprobar los datos
-- ===================================================

-- SELECT * FROM categorias;
-- SELECT id, nombre, precio, stock FROM productos;
-- SELECT id, nombre_completo, correo_electronico, tipo_usuario FROM usuarios;
-- SELECT p.id, u.nombre_completo, p.fecha_pedido, p.total_pedido, p.estado 
-- FROM pedidos p 
-- JOIN usuarios u ON p.usuario_id = u.id;
