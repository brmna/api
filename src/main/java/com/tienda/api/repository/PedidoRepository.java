package com.tienda.api.repository;

import com.tienda.api.model.Pedido;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Acceso a datos de la tabla {@code pedidos} y, en cascada, de {@code detalle_pedidos}.
 */
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    /**
     * Lista todos los pedidos cargando sus ítems y productos en una sola consulta.
     *
     * @return lista de pedidos con su detalle
     */
    @Override
    @EntityGraph(attributePaths = {"items", "items.producto"})
    List<Pedido> findAll();

    /**
     * Busca un pedido por id cargando sus ítems y productos.
     *
     * @param id identificador del pedido
     * @return el pedido, si existe
     */
    @Override
    @EntityGraph(attributePaths = {"items", "items.producto"})
    Optional<Pedido> findById(Long id);

    /**
     * Indica si algún pedido contiene el producto indicado.
     *
     * @param productoId identificador del producto
     * @return {@code true} si el producto forma parte de al menos un pedido
     */
    boolean existsByItemsProductoId(Long productoId);
}
