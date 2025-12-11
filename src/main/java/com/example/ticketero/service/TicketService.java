package com.example.ticketero.service;

import com.example.ticketero.model.dto.request.ConsultarTicketRequest;
import com.example.ticketero.model.dto.request.CrearTicketRequest;
import com.example.ticketero.model.dto.response.TicketResponse;
import com.example.ticketero.model.entity.*;
import com.example.ticketero.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class TicketService {

    private final TicketRepository ticketRepository;
    private final ColaService colaService;
    private final NotificacionService notificacionService;

    @Transactional
    public TicketResponse crearTicket(CrearTicketRequest request) {
        log.info("Creando ticket para nationalId: {}", request.nationalId());

        Ticket ticket = Ticket.builder()
            .codigoReferencia(generarCodigoReferencia())
            .nationalId(request.nationalId())
            .telefono(request.telefono())
            .tipoServicio(request.tipoServicio())
            .sucursalId(request.sucursalId())
            .status(TicketStatus.PENDIENTE)
            .build();

        Ticket saved = ticketRepository.save(ticket);

        // Calcular posición y tiempo estimado
        colaService.actualizarPosicionYTiempo(saved);

        // Programar notificación de confirmación
        notificacionService.programarNotificacionConfirmacion(saved);

        log.info("Ticket creado: {}", saved.getCodigoReferencia());
        return toResponse(saved);
    }

    public Optional<TicketResponse> consultarTicket(ConsultarTicketRequest request) {
        return ticketRepository.findByCodigoReferenciaAndNationalId(
            request.codigoReferencia(), request.nationalId())
            .map(this::toResponse);
    }

    @Transactional
    public void asignarEjecutivo(Long ticketId, Long ejecutivoId) {
        Ticket ticket = ticketRepository.findById(ticketId)
            .orElseThrow(() -> new RuntimeException("Ticket no encontrado"));

        ticket.setEjecutivoId(ejecutivoId);
        ticket.setStatus(TicketStatus.EN_ATENCION);
        ticket.setAtendidoAt(LocalDateTime.now());

        notificacionService.programarNotificacionLlamada(ticket);
        log.info("Ticket {} asignado a ejecutivo {}", ticketId, ejecutivoId);
    }

    @Transactional
    public void finalizarTicket(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
            .orElseThrow(() -> new RuntimeException("Ticket no encontrado"));

        ticket.setStatus(TicketStatus.FINALIZADO);
        ticket.setFinalizadoAt(LocalDateTime.now());

        notificacionService.programarNotificacionFinalizacion(ticket);
        log.info("Ticket {} finalizado", ticketId);
    }

    private String generarCodigoReferencia() {
        return String.format("%06d", new Random().nextInt(999999));
    }

    private TicketResponse toResponse(Ticket ticket) {
        return new TicketResponse(
            ticket.getId(),
            ticket.getCodigoReferencia(),
            ticket.getNationalId(),
            ticket.getTelefono(),
            ticket.getStatus(),
            ticket.getTipoServicio(),
            ticket.getSucursalId(),
            ticket.getEjecutivoId(),
            ticket.getPosicionCola(),
            ticket.getTiempoEstimadoMinutos(),
            ticket.getCreatedAt(),
            ticket.getAtendidoAt(),
            ticket.getFinalizadoAt()
        );
    }
}