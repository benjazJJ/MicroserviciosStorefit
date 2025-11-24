package com.storefit.catalog_service.Controller;

import com.storefit.catalog_service.Model.Categoria;
import com.storefit.catalog_service.Service.CategoriaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.web.bind.annotation.RequestHeader;

import com.storefit.catalog_service.security.Authorization;
import com.storefit.catalog_service.security.RequestUser;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/categorias")
@RequiredArgsConstructor
public class CategoriaController {

    private final CategoriaService service;

    @Operation(summary = "Listar categorias", description = "Obtiene todas las categorias")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Categoria.class))))
    })
    @GetMapping
    public ResponseEntity<List<Categoria>> all(
            @RequestHeader("X-User-Rut") String headerRut,   // Header con RUT autenticado
            @RequestHeader("X-User-Rol") String headerRol) { // Header con rol autenticado
        RequestUser user = Authorization.fromHeaders(headerRut, headerRol); // Valida headers (401 si faltan)
        Authorization.requireAdmin(user); // Solo ADMIN puede acceder
        return ResponseEntity.ok(service.findAll());
    }

    @Operation(summary = "Obtener categoria por ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = Categoria.class))),
        @ApiResponse(responseCode = "404", description = "No encontrada")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Categoria> byId(@PathVariable Long id,
            @RequestHeader("X-User-Rut") String headerRut,   // Header con RUT autenticado
            @RequestHeader("X-User-Rol") String headerRol) { // Header con rol autenticado
        RequestUser user = Authorization.fromHeaders(headerRut, headerRol); // Valida headers
        Authorization.requireAdmin(user); // Solo ADMIN
        return ResponseEntity.ok(service.findById(id));
    }

    @Operation(summary = "Crear categoria")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Creada"),
        @ApiResponse(responseCode = "400", description = "Solicitud invalida")
    })
    @PostMapping
    public ResponseEntity<Map<String, Object>> create(
            @RequestHeader("X-User-Rut") String headerRut,   // Header con RUT autenticado
            @RequestHeader("X-User-Rol") String headerRol,  // Header con rol autenticado
            @Valid @RequestBody Categoria c) {
        RequestUser user = Authorization.fromHeaders(headerRut, headerRol); // Valida headers
        Authorization.requireAdmin(user); // Solo ADMIN
        var created = service.create(c);
        var location = URI.create("/api/v1/categorias/" + created.getIdCategoria());
        return ResponseEntity.created(location).body(
            Map.of(
                "message", "Categoria creada correctamente",
                "data", created
            )
        );
    }

    @Operation(summary = "Actualizar categoria")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Actualizada"),
        @ApiResponse(responseCode = "404", description = "No encontrada")
    })
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> update(
            @PathVariable Long id,
            @RequestHeader("X-User-Rut") String headerRut,   // Header con RUT autenticado
            @RequestHeader("X-User-Rol") String headerRol,  // Header con rol autenticado
            @Valid @RequestBody Categoria c) {
        RequestUser user = Authorization.fromHeaders(headerRut, headerRol); // Valida headers
        Authorization.requireAdmin(user); // Solo ADMIN
        var updated = service.update(id, c);
        return ResponseEntity.ok(
            Map.of(
                "message", "Categoria actualizada correctamente",
                "data", updated
            )
        );
    }

    @Operation(summary = "Eliminar categoria")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Eliminada"),
        @ApiResponse(responseCode = "404", description = "No encontrada")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(
            @PathVariable Long id,
            @RequestHeader("X-User-Rut") String headerRut,   // Header con RUT autenticado
            @RequestHeader("X-User-Rol") String headerRol) { // Header con rol autenticado
        RequestUser user = Authorization.fromHeaders(headerRut, headerRol); // Valida headers
        Authorization.requireAdmin(user); // Solo ADMIN
        service.delete(id);
        return ResponseEntity.ok(
            Map.of("message", "Categoria eliminada correctamente")
        );
    }

    // Manejo de errores local al controlador
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(EntityNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("message", ex.getMessage() != null ? ex.getMessage() : "Recurso no encontrado"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", ex.getMessage() != null ? ex.getMessage() : "Solicitud invalida"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .orElse("Datos invalidos");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", message));
    }
}
