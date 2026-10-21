package com.tienda.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada de la API de la tienda.
 * Expone los módulos de Productos y Pedidos (carrito de compras) sobre PostgreSQL.
 */
@SpringBootApplication
public class ApiApplication {

	/**
	 * Inicia la aplicación Spring Boot.
	 *
	 * @param args argumentos de línea de comandos
	 */
	public static void main(String[] args) {
		SpringApplication.run(ApiApplication.class, args);
	}

}
