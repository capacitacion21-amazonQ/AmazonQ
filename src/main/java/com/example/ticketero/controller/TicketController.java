package com.example.ticketero.controller;

import com.example.ticketero.model.dto.request.ConsultarTicketRequest;
import com.example.ticketero.model.dto.request.CrearTicketRequest;
import com.example.ticketero.model.dto.response.TicketResponse;
import com.example.ticketero.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
@Slf4j
public class TicketController {

    private final TicketService ticketService;

    @PostMapping
    public ResponseEntity<TicketResponse> crearTicket(
        @Valid @RequestBody CrearTicketRequest request
    ) {
        log.info("POST /api/tickets - nationalId: {}", request.nationalId());
        TicketResponse response = ticketService.crearTicket(request);
        return ResponseEntity.status(201).body(response);
    }

    @PostMapping("/consultar")
    public ResponseEntity<TicketResponse> consultarTicket(
        @Valid @RequestBody ConsultarTicketRequest request
    ) {
        log.info("POST /api/tickets/consultar - codigo: {}", request.codigoReferencia());
        return ticketService.consultarTicket(request)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/asignar/{ejecutivoId}")
    public ResponseEntity<Void> asignarEjecutivo(
        @PathVariable Long id,
        @PathVariable Long ejecutivoId
    ) {
        log.info("PUT /api/tickets/{}/asignar/{}", id, ejecutivoId);
        ticketService.asignarEjecutivo(id, ejecutivoId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/finalizar")
    public ResponseEntity<Void> finalizarTicket(@PathVariable Long id) {
        log.info("PUT /api/tickets/{}/finalizar", id);
        ticketService.finalizarTicket(id);
        return ResponseEntity.noContent().build();
    }
}