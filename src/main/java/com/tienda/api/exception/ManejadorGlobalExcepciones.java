package com.tienda.api.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Map;

/**
 * Convierte las excepciones de ambos módulos en respuestas HTTP con el formato
 * {@code {"error": "mensaje"}}.
 */
@RestControllerAdvice
@Slf4j
public class ManejadorGlobalExcepciones {

    /**
     * Responde 404 cuando el recurso solicitado no existe.
     *
     * @param ex excepción lanzada por el servicio
     * @return respuesta 404 con el mensaje de error
     */
    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<Map<String, String>> noEncontrado(RecursoNoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
    }

    /**
     * Responde 400 ante violaciones de reglas de negocio.
     *
     * @param ex excepción lanzada por el servicio
     * @return respuesta 400 con el mensaje de error
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> solicitudInvalida(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }

    /**
     * Responde 400 cuando el cuerpo de la petición no cumple las validaciones.
     *
     * @param ex excepción de validación de Spring
     * @return respuesta 400 con el primer mensaje de validación
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> validacion(MethodArgumentNotValidException ex) {
        FieldError campo = ex.getBindingResult().getFieldError();
        String mensaje = campo == null ? "La solicitud no es válida" : campo.getDefaultMessage();
        return ResponseEntity.badRequest().body(Map.of("error", mensaje));
    }

    /**
     * Responde 400 cuando una entidad no cumple las validaciones al guardarse.
     *
     * @param ex excepción de Bean Validation
     * @return respuesta 400 con el mensaje de validación
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, String>> restriccion(ConstraintViolationException ex) {
        String mensaje = ex.getConstraintViolations().stream()
                .map(v -> v.getMessage())
                .findFirst()
                .orElse("La solicitud no es válida");
        return ResponseEntity.badRequest().body(Map.of("error", mensaje));
    }

    /**
     * Responde 400 cuando el JSON enviado está mal formado.
     *
     * @param ex excepción de lectura del cuerpo
     * @return respuesta 400 con un mensaje genérico
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> jsonInvalido(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", "El cuerpo de la petición no es un JSON válido"));
    }

    /**
     * Responde 400 cuando un parámetro de la ruta tiene un tipo incorrecto (por ejemplo, /api/productos/abc).
     *
     * @param ex excepción de conversión de tipos
     * @return respuesta 400 con el nombre del parámetro inválido
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, String>> tipoInvalido(MethodArgumentTypeMismatchException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", "El parámetro '" + ex.getName() + "' no es válido"));
    }

    /**
     * Responde 409 cuando una restricción de integridad impide la operación,
     * por ejemplo al eliminar un producto incluido en un pedido.
     *
     * @param ex excepción de integridad de datos
     * @return respuesta 409 con el mensaje de error
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> conflicto(DataIntegrityViolationException ex) {
        log.warn("Conflicto de integridad: {}", ex.getMessage());
        // Si la lanzó el propio servicio, su mensaje ya es claro; si viene de la base de datos, se usa uno genérico
        String mensaje = ex.getCause() == null
                ? ex.getMessage()
                : "La operación viola una restricción de integridad de la base de datos";
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", mensaje));
    }
}
