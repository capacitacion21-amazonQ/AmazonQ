-- Crear tabla tickets
CREATE TABLE tickets (
    id BIGSERIAL PRIMARY KEY,
    codigo_referencia VARCHAR(10) UNIQUE NOT NULL,
    national_id VARCHAR(20) NOT NULL,
    telefono VARCHAR(15) NOT NULL,
    chat_id VARCHAR(50),
    status VARCHAR(20) NOT NULL,
    tipo_servicio VARCHAR(30) NOT NULL,
    sucursal_id BIGINT NOT NULL,
    ejecutivo_id BIGINT,
    posicion_cola INTEGER,
    tiempo_estimado_minutos INTEGER,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    atendido_at TIMESTAMP,
    finalizado_at TIMESTAMP
);

-- Índices para performance
CREATE INDEX idx_tickets_codigo_national ON tickets(codigo_referencia, national_id);
CREATE INDEX idx_tickets_sucursal_status ON tickets(sucursal_id, status);
CREATE INDEX idx_tickets_sucursal_tipo_status ON tickets(sucursal_id, tipo_servicio, status);
CREATE INDEX idx_tickets_status_ejecutivo ON tickets(status, ejecutivo_id);
CREATE INDEX idx_tickets_created_at ON tickets(created_at DESC);