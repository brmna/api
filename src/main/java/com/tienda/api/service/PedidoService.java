package com.tienda.api.service;

import com.tienda.api.dto.ItemRequest;
import com.tienda.api.dto.PedidoRequest;
import com.tienda.api.exception.RecursoNoEncontradoException;
import com.tienda.api.model.DetallePedido;
import com.tienda.api.model.EstadoPedido;
import com.tienda.api.model.Pedido;
import com.tienda.api.model.Producto;
import com.tienda.api.repository.PedidoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Lógica de negocio del módulo de Pedidos (carrito de compras).
 * Calcula subtotales y totales en el servidor, controla los estados
 * y descuenta el inventario al confirmar.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ProductoService productoService;

    /**
     * Lista todos los pedidos con su detalle.
     *
     * @return lista de pedidos
     */
    @Transactional(readOnly = true)
    public List<Pedido> listarTodos() {
        return pedidoRepository.findAll();
    }

    /**
     * Busca un pedido por su identificador.
     *
     * @param id identificador del pedido
     * @return el pedido con sus ítems
     * @throws RecursoNoEncontradoException si no existe
     */
    @Transactional(readOnly = true)
    public Pedido buscarPorId(Long id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Pedido no encontrado con id " + id));
    }

    /**
     * Crea un pedido en estado CARRITO con los ítems indicados.
     *
     * @param request cliente e ítems del pedido
     * @return el pedido guardado con subtotales y total calculados
     */
    @Transactional
    public Pedido crear(PedidoRequest request) {
        Pedido pedido = Pedido.builder().cliente(request.getCliente()).build();
        request.getItems().forEach(item -> pedido.agregarItem(construirItem(item)));
        pedido.recalcularTotal();
        log.info("Pedido creado para el cliente {}", pedido.getCliente());
        return pedidoRepository.save(pedido);
    }

    /**
     * Reemplaza el cliente y todos los ítems de un pedido en estado CARRITO.
     *
     * @param id      identificador del pedido
     * @param request nuevos datos del pedido
     * @return el pedido actualizado
     * @throws RecursoNoEncontradoException si el pedido o algún producto no existe
     * @throws IllegalArgumentException     si el pedido no está en estado CARRITO
     */
    @Transactional
    public Pedido actualizar(Long id, PedidoRequest request) {
        Pedido pedido = buscarCarrito(id);
        pedido.setCliente(request.getCliente());
        pedido.getItems().clear();
        request.getItems().forEach(item -> pedido.agregarItem(construirItem(item)));
        pedido.recalcularTotal();
        log.info("Pedido {} actualizado", id);
        return pedidoRepository.save(pedido);
    }

    /**
     * Agrega un producto al carrito.
     *
     * @param id      identificador del pedido
     * @param request producto y cantidad a agregar
     * @return el pedido con el total actualizado
     * @throws RecursoNoEncontradoException si el pedido o el producto no existe
     * @throws IllegalArgumentException     si el pedido no está en CARRITO o la cantidad no es válida
     */
    @Transactional
    public Pedido agregarItem(Long id, ItemRequest request) {
        Pedido pedido = buscarCarrito(id);
        pedido.agregarItem(construirItem(request));
        pedido.recalcularTotal();
        log.info("Producto {} agregado al pedido {}", request.getProductoId(), id);
        return pedidoRepository.save(pedido);
    }

    /**
     * Retira un ítem del carrito.
     *
     * @param id     identificador del pedido
     * @param itemId identificador del ítem (detalle) a retirar
     * @return el pedido con el total actualizado
     * @throws RecursoNoEncontradoException si el pedido o el ítem no existe
     * @throws IllegalArgumentException     si el pedido no está en estado CARRITO
     */
    @Transactional
    public Pedido quitarItem(Long id, Long itemId) {
        Pedido pedido = buscarCarrito(id);
        boolean eliminado = pedido.getItems().removeIf(item -> item.getId().equals(itemId));
        if (!eliminado) {
            throw new RecursoNoEncontradoException("Ítem no encontrado con id " + itemId);
        }
        pedido.recalcularTotal();
        log.info("Ítem {} retirado del pedido {}", itemId, id);
        return pedidoRepository.save(pedido);
    }

    /**
     * Confirma la compra: valida que haya stock suficiente para todos los ítems
     * y luego lo descuenta. Si algún producto no alcanza, no se modifica ningún stock.
     *
     * @param id identificador del pedido
     * @return el pedido en estado CONFIRMADO
     * @throws IllegalArgumentException si el carrito está vacío, no está en CARRITO o falta stock
     */
    @Transactional
    public Pedido confirmar(Long id) {
        Pedido pedido = buscarCarrito(id);
        if (pedido.getItems().isEmpty()) {
            throw new IllegalArgumentException("El carrito está vacío");
        }
        // Se valida todo el stock antes de descontar para no dejar cambios a medias
        pedido.getItems().forEach(item -> {
            Producto producto = item.getProducto();
            if (producto.getStock() < item.getCantidad()) {
                throw new IllegalArgumentException("Stock insuficiente para " + producto.getNombre()
                        + " (disponible: " + producto.getStock() + ", solicitado: " + item.getCantidad() + ")");
            }
        });
        pedido.getItems().forEach(item -> {
            Producto producto = item.getProducto();
            producto.setStock(producto.getStock() - item.getCantidad());
        });
        pedido.setEstado(EstadoPedido.CONFIRMADO);
        log.info("Pedido {} confirmado", id);
        return pedidoRepository.save(pedido);
    }

    /**
     * Elimina un pedido y, en cascada, todos sus ítems.
     *
     * @param id identificador del pedido
     * @throws RecursoNoEncontradoException si no existe
     */
    @Transactional
    public void eliminar(Long id) {
        pedidoRepository.delete(buscarPorId(id));
        log.info("Pedido {} eliminado", id);
    }

    private Pedido buscarCarrito(Long id) {
        Pedido pedido = buscarPorId(id);
        if (pedido.getEstado() != EstadoPedido.CARRITO) {
            throw new IllegalArgumentException("Solo se pueden modificar pedidos en estado CARRITO");
        }
        return pedido;
    }

    private DetallePedido construirItem(ItemRequest request) {
        if (request.getCantidad() == null || request.getCantidad() <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que cero");
        }
        Producto producto = productoService.buscarPorId(request.getProductoId());
        return DetallePedido.builder()
                .producto(producto)
                .cantidad(request.getCantidad())
                .precioUnitario(producto.getPrecio())
                .subtotal(producto.getPrecio().multiply(BigDecimal.valueOf(request.getCantidad())))
                .build();
    }
}
