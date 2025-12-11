package com.example.ticketero.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ConsultarTicketRequest(
    @NotBlank(message = "Código de referencia es requerido")
    @Size(min = 6, max = 10, message = "Código debe tener entre 6-10 caracteres")
    String codigoReferencia,

    @NotBlank(message = "National ID es requerido")
    @Size(min = 8, max = 20, message = "National ID debe tener entre 8-20 caracteres")
    String nationalId
) {}