package com.tienda.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Datos de entrada para agregar un producto al carrito.
 * El precio y el subtotal no se reciben: los calcula el servidor.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemRequest {

    @NotNull(message = "El productoId es requerido")
    private Long productoId;

    @NotNull(message = "La cantidad es requerida")
    @Positive(message = "La cantidad debe ser mayor que cero")
    private Integer cantidad;
}
