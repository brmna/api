package com.tienda.api.repository;

import com.tienda.api.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Acceso a datos de la tabla {@code productos}.
 */
public interface ProductoRepository extends JpaRepository<Producto, Long> {
}
