package com.tienda.api.service;

import com.tienda.api.exception.RecursoNoEncontradoException;
import com.tienda.api.model.Producto;
import com.tienda.api.repository.PedidoRepository;
import com.tienda.api.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Lógica de negocio del módulo de Productos.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final PedidoRepository pedidoRepository;

    /**
     * Lista todos los productos.
     *
     * @return lista de productos (vacía si no hay registros)
     */
    public List<Producto> listarTodos() {
        return productoRepository.findAll();
    }

    /**
     * Busca un producto por su identificador.
     *
     * @param id identificador del producto
     * @return el producto encontrado
     * @throws RecursoNoEncontradoException si no existe
     */
    public Producto buscarPorId(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado con id " + id));
    }

    /**
     * Crea un producto nuevo. Cualquier id enviado por el cliente se ignora.
     *
     * @param producto datos del producto
     * @return el producto guardado con su id asignado
     */
    @Transactional
    public Producto crear(Producto producto) {
        producto.setId(null);
        log.info("Creando producto: {}", producto.getNombre());
        return productoRepository.save(producto);
    }

    /**
     * Actualiza nombre, descripción, precio y stock de un producto existente.
     *
     * @param id    identificador del producto
     * @param datos nuevos valores
     * @return el producto actualizado
     * @throws RecursoNoEncontradoException si no existe
     */
    @Transactional
    public Producto actualizar(Long id, Producto datos) {
        Producto existente = buscarPorId(id);
        existente.setNombre(datos.getNombre());
        existente.setDescripcion(datos.getDescripcion());
        existente.setPrecio(datos.getPrecio());
        existente.setStock(datos.getStock());
        log.info("Actualizando producto {}", id);
        return productoRepository.save(existente);
    }

    /**
     * Elimina un producto que no forme parte de ningún pedido.
     *
     * @param id identificador del producto
     * @throws RecursoNoEncontradoException   si no existe
     * @throws DataIntegrityViolationException si el producto está en algún pedido (409)
     */
    @Transactional
    public void eliminar(Long id) {
        Producto producto = buscarPorId(id);
        if (pedidoRepository.existsByItemsProductoId(id)) {
            throw new DataIntegrityViolationException("El producto está asociado a uno o más pedidos");
        }
        log.info("Eliminando producto {}", id);
        productoRepository.delete(producto);
    }
}
