package com.example.ticketero.service;

import com.example.ticketero.model.entity.Ticket;
import com.example.ticketero.model.entity.TicketStatus;
import com.example.ticketero.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ColaService {

    private final TicketRepository ticketRepository;
    private static final int TIEMPO_PROMEDIO_ATENCION = 15; // minutos

    public void actualizarPosicionYTiempo(Ticket ticket) {
        long ticketsAnteriores = ticketRepository.countTicketsAnteriores(
            ticket.getSucursalId(),
            ticket.getTipoServicio(),
            TicketStatus.PENDIENTE,
            ticket.getCreatedAt()
        );

        int posicion = (int) ticketsAnteriores + 1;
        int tiempoEstimado = posicion * TIEMPO_PROMEDIO_ATENCION;

        ticket.setPosicionCola(posicion);
        ticket.setTiempoEstimadoMinutos(tiempoEstimado);

        log.debug("Ticket {} - Posición: {}, Tiempo estimado: {} min",
            ticket.getCodigoReferencia(), posicion, tiempoEstimado);
    }
}