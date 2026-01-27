-- ========================================
-- TABLA: FACTURAS
-- ========================================
-- Almacena las facturas generadas para cada pedido

CREATE TABLE IF NOT EXISTS facturas (
    id BIGSERIAL PRIMARY KEY,
    codigo_factura VARCHAR(50) UNIQUE NOT NULL,
    pedido_id BIGINT NOT NULL,
    usuario_id BIGINT NOT NULL,
    fecha_emision TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    total DECIMAL(10,2) NOT NULL,
    ruta_pdf VARCHAR(255),
    
    CONSTRAINT fk_factura_pedido FOREIGN KEY (pedido_id) REFERENCES pedidos(id) ON DELETE CASCADE,
    CONSTRAINT fk_factura_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE
);

-- Índices para mejorar rendimiento
CREATE INDEX IF NOT EXISTS idx_facturas_usuario ON facturas(usuario_id);
CREATE INDEX IF NOT EXISTS idx_facturas_codigo ON facturas(codigo_factura);
CREATE INDEX IF NOT EXISTS idx_facturas_fecha ON facturas(fecha_emision DESC);

-- Comentarios
COMMENT ON TABLE facturas IS 'Facturas generadas para los pedidos de HÁBILIS';
COMMENT ON COLUMN facturas.codigo_factura IS 'Código único de factura (FAC-YYYYMMDD-XXXXX)';
COMMENT ON COLUMN facturas.ruta_pdf IS 'Ruta opcional donde se almacena el PDF (si se guarda en disco)';
