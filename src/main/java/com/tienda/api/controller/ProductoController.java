package com.tienda.api.controller;

import com.tienda.api.model.Producto;
import com.tienda.api.service.ProductoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Endpoints REST del módulo de Productos ({@code /api/productos}).
 */
@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;

    /**
     * Lista todos los productos.
     *
     * @return 200 con el arreglo de productos
     */
    @GetMapping
    public ResponseEntity<List<Producto>> listar() {
        return ResponseEntity.ok(productoService.listarTodos());
    }

    /**
     * Consulta un producto por id.
     *
     * @param id identificador del producto
     * @return 200 con el producto, o 404 si no existe
     */
    @GetMapping("/{id}")
    public ResponseEntity<Producto> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(productoService.buscarPorId(id));
    }

    /**
     * Crea un producto.
     *
     * @param producto datos del producto
     * @return 201 con el producto creado, o 400 si los datos no son válidos
     */
    @PostMapping
    public ResponseEntity<Producto> crear(@Valid @RequestBody Producto producto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productoService.crear(producto));
    }

    /**
     * Actualiza un producto existente.
     *
     * @param id       identificador del producto
     * @param producto nuevos datos
     * @return 200 con el producto actualizado, 400 si los datos no son válidos o 404 si no existe
     */
    @PutMapping("/{id}")
    public ResponseEntity<Producto> actualizar(@PathVariable Long id, @Valid @RequestBody Producto producto) {
        return ResponseEntity.ok(productoService.actualizar(id, producto));
    }

    /**
     * Elimina un producto.
     *
     * @param id identificador del producto
     * @return 204 si se eliminó, 404 si no existe o 409 si forma parte de un pedido
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        productoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
