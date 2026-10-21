package com.tienda.api.exception;

import lombok.experimental.StandardException;

/**
 * Indica que el recurso solicitado (producto, pedido o ítem) no existe.
 * El manejador global la traduce a una respuesta 404.
 * Lombok genera los constructores estándar con {@code @StandardException}.
 */
@StandardException
public class RecursoNoEncontradoException extends RuntimeException {
}
