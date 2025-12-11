package com.example.ticketero.model.dto.request;

import com.example.ticketero.model.entity.TipoServicio;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CrearTicketRequest(
    @NotBlank(message = "National ID es requerido")
    @Size(min = 8, max = 20, message = "National ID debe tener entre 8-20 caracteres")
    String nationalId,

    @NotBlank(message = "Teléfono es requerido")
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Formato de teléfono inválido")
    String telefono,

    @NotNull(message = "Tipo de servicio es requerido")
    TipoServicio tipoServicio,

    @NotNull(message = "Sucursal ID es requerido")
    Long sucursalId
) {}