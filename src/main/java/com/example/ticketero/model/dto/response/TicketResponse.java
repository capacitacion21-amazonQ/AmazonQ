package com.example.ticketero.model.dto.response;

import com.example.ticketero.model.entity.TicketStatus;
import com.example.ticketero.model.entity.TipoServicio;

import java.time.LocalDateTime;

public record TicketResponse(
    Long id,
    String codigoReferencia,
    String nationalId,
    String telefono,
    TicketStatus status,
    TipoServicio tipoServicio,
    Long sucursalId,
    Long ejecutivoId,
    Integer posicionCola,
    Integer tiempoEstimadoMinutos,
    LocalDateTime createdAt,
    LocalDateTime atendidoAt,
    LocalDateTime finalizadoAt
) {}