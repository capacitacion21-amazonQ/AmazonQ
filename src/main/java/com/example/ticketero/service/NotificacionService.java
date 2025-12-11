package com.example.ticketero.service;

import com.example.ticketero.model.entity.Mensaje;
import com.example.ticketero.model.entity.Ticket;
import com.example.ticketero.model.entity.TipoMensaje;
import com.example.ticketero.repository.MensajeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class NotificacionService {

    private final MensajeRepository mensajeRepository;

    @Transactional
    public void programarNotificacionConfirmacion(Ticket ticket) {
        String contenido = String.format(
            "✅ Ticket creado: %s\n" +
            "🏦 Sucursal: %d\n" +
            "📋 Servicio: %s\n" +
            "📍 Posición: %d\n" +
            "⏱️ Tiempo estimado: %d min",
            ticket.getCodigoReferencia(),
            ticket.getSucursalId(),
            ticket.getTipoServicio(),
            ticket.getPosicionCola(),
            ticket.getTiempoEstimadoMinutos()
        );

        crearMensaje(ticket, contenido, TipoMensaje.CONFIRMACION_TICKET);
    }

    @Transactional
    public void programarNotificacionLlamada(Ticket ticket) {
        String contenido = String.format(
            "🔔 ¡Es tu turno!\n" +
            "Ticket: %s\n" +
            "Dirígete al ejecutivo asignado",
            ticket.getCodigoReferencia()
        );

        crearMensaje(ticket, contenido, TipoMensaje.LLAMADA_ATENCION);
    }

    @Transactional
    public void programarNotificacionFinalizacion(Ticket ticket) {
        String contenido = String.format(
            "✅ Atención finalizada\n" +
            "Ticket: %s\n" +
            "Gracias por usar nuestros servicios",
            ticket.getCodigoReferencia()
        );

        crearMensaje(ticket, contenido, TipoMensaje.TICKET_FINALIZADO);
    }

    private void crearMensaje(Ticket ticket, String contenido, TipoMensaje tipo) {
        Mensaje mensaje = Mensaje.builder()
            .ticket(ticket)
            .contenido(contenido)
            .tipo(tipo)
            .enviado(false)
            .build();

        mensajeRepository.save(mensaje);
        log.debug("Mensaje programado para ticket {}: {}", ticket.getCodigoReferencia(), tipo);
    }
}