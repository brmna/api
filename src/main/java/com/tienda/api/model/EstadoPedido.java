package com.tienda.api.model;

/**
 * Estados por los que atraviesa un pedido.
 * Solo los pedidos en {@link #CARRITO} pueden modificarse.
 */
public enum EstadoPedido {
    CARRITO,
    CONFIRMADO,
    CANCELADO
}
