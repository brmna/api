package com.tienda.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
 
/**
 * Datos de entrada para crear o actualizar un pedido (carrito).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PedidoRequest {

    @NotBlank(message = "El cliente es requerido")
    @Size(max = 100, message = "El cliente no puede superar 100 caracteres")
    private String cliente;

    @NotEmpty(message = "El pedido debe tener al menos un ítem")
    private List<@Valid ItemRequest> items;
}
