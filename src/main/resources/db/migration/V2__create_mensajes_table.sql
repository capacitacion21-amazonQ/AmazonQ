-- Crear tabla mensajes
CREATE TABLE mensajes (
    id BIGSERIAL PRIMARY KEY,
    ticket_id BIGINT NOT NULL,
    contenido VARCHAR(500) NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    enviado BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    enviado_at TIMESTAMP,
    CONSTRAINT fk_mensajes_ticket FOREIGN KEY (ticket_id) 
        REFERENCES tickets(id) ON DELETE CASCADE
);

-- Índices para performance
CREATE INDEX idx_mensajes_ticket_id ON mensajes(ticket_id);
CREATE INDEX idx_mensajes_enviado ON mensajes(enviado);
CREATE INDEX idx_mensajes_created_at ON mensajes(created_at ASC);