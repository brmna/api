package com.tienda.api.controller;

import com.tienda.api.dto.ItemRequest;
import com.tienda.api.dto.PedidoRequest;
import com.tienda.api.model.Pedido;
import com.tienda.api.service.PedidoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Endpoints REST del módulo de Pedidos ({@code /api/pedidos}).
 */
@RestController
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
public class PedidoController {

    private final PedidoService pedidoService;

    /**
     * Lista todos los pedidos con su detalle.
     *
     * @return 200 con el arreglo de pedidos
     */
    @GetMapping
    public ResponseEntity<List<Pedido>> listar() {
        return ResponseEntity.ok(pedidoService.listarTodos());
    }

    /**
     * Consulta un pedido con sus ítems, subtotales y total.
     *
     * @param id identificador del pedido
     * @return 200 con el pedido, o 404 si no existe
     */
    @GetMapping("/{id}")
    public ResponseEntity<Pedido> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(pedidoService.buscarPorId(id));
    }

    /**
     * Crea un pedido (carrito) con sus ítems.
     *
     * @param request cliente e ítems
     * @return 201 con el pedido creado, o 400 si los datos no son válidos
     */
    @PostMapping
    public ResponseEntity<Pedido> crear(@Valid @RequestBody PedidoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pedidoService.crear(request));
    }

    /**
     * Reemplaza el cliente y los ítems de un carrito.
     *
     * @param id      identificador del pedido
     * @param request nuevos datos
     * @return 200 con el pedido actualizado, 400 si no está en CARRITO o 404 si no existe
     */
    @PutMapping("/{id}")
    public ResponseEntity<Pedido> actualizar(@PathVariable Long id, @Valid @RequestBody PedidoRequest request) {
        return ResponseEntity.ok(pedidoService.actualizar(id, request));
    }

    /**
     * Elimina un pedido y sus ítems.
     *
     * @param id identificador del pedido
     * @return 204 si se eliminó, o 404 si no existe
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        pedidoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Agrega un producto al carrito.
     *
     * @param id      identificador del pedido
     * @param request producto y cantidad
     * @return 200 con el pedido actualizado, 400 si la cantidad o el estado no son válidos, 404 si no existe
     */
    @PostMapping("/{id}/items")
    public ResponseEntity<Pedido> agregarItem(@PathVariable Long id, @Valid @RequestBody ItemRequest request) {
        return ResponseEntity.ok(pedidoService.agregarItem(id, request));
    }

    /**
     * Retira un producto del carrito.
     *
     * @param id     identificador del pedido
     * @param itemId identificador del ítem
     * @return 200 con el pedido actualizado, o 404 si el pedido o el ítem no existe
     */
    @DeleteMapping("/{id}/items/{itemId}")
    public ResponseEntity<Pedido> quitarItem(@PathVariable Long id, @PathVariable Long itemId) {
        return ResponseEntity.ok(pedidoService.quitarItem(id, itemId));
    }

    /**
     * Confirma la compra y descuenta el inventario.
     *
     * @param id identificador del pedido
     * @return 200 con el pedido confirmado, o 400 si no hay stock suficiente o no está en CARRITO
     */
    @PatchMapping("/{id}/confirmar")
    public ResponseEntity<Pedido> confirmar(@PathVariable Long id) {
        return ResponseEntity.ok(pedidoService.confirmar(id));
    }
}
