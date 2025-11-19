package com.storefit.catalog_service.Controller;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.storefit.catalog_service.Model.Producto;
import com.storefit.catalog_service.Model.ProductoId;
import com.storefit.catalog_service.Service.ProductoService;
import com.storefit.catalog_service.Service.StockReservaItem;
import com.storefit.catalog_service.Service.StockInsuficienteException;
 

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import jakarta.persistence.EntityNotFoundException;

@RestController
@RequestMapping("/api/v1/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService service;

    @Operation(summary = "Listar productos", description = "Obtiene todos los productos del catálogo")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = Producto.class)))
    })
    @GetMapping
    public ResponseEntity<List<Producto>> all() {
        return ResponseEntity.ok(service.findAll());
    }

    @Operation(summary = "Obtener producto por ID compuesto",
            description = "Busca un producto por (id_categoria, id_producto)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Encontrado",
            content = @Content(schema = @Schema(implementation = Producto.class))),
        @ApiResponse(responseCode = "404", description = "No encontrado")
    })
    @GetMapping("/{categoriaId}/{productoId}")
    public ResponseEntity<Producto> byId(@PathVariable Long categoriaId, @PathVariable Long productoId) {
        return ResponseEntity.ok(service.findByIds(categoriaId, productoId));
    }

    @Operation(summary = "Listar productos por categoría",
            description = "Devuelve los productos pertenecientes a la categoría indicada")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = Producto.class)))
    })
    @GetMapping("/categoria/{categoriaId}")
    public ResponseEntity<List<Producto>> byCategoria(@PathVariable Long categoriaId) {
        return ResponseEntity.ok(service.findByCategoria(categoriaId));
    }

    @Operation(summary = "Crear producto",
            description = "Crea un nuevo producto con id compuesto (id_categoria + id_producto). Requiere que la categoría exista.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Creado",
            content = @Content(schema = @Schema(implementation = Producto.class))),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "404", description = "Categoría no encontrada"),
        @ApiResponse(responseCode = "409", description = "Producto duplicado")
    })
    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@Valid @RequestBody Producto p) {
        var created = service.create(p);
        ProductoId id = created.getId();
        var location = URI.create("/api/v1/productos/" + id.getIdCategoria() + "/" + id.getIdProducto());
        return ResponseEntity.created(location).body(
            Map.of(
                "message", "Producto añadido correctamente",
                "data", created
            )
        );
    }

    @Operation(summary = "Actualizar producto",
            description = "Actualiza los datos del producto identificado por id_categoria e id_producto")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Actualizado",
            content = @Content(schema = @Schema(implementation = Producto.class))),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "404", description = "No encontrado")
    })
    @PutMapping("/{categoriaId}/{productoId}")
    public ResponseEntity<Map<String, Object>> update(
            @PathVariable Long categoriaId,
            @PathVariable Long productoId,
            @Valid @RequestBody Producto p) {

        var updated = service.update(categoriaId, productoId, p);
        return ResponseEntity.ok(
            Map.of(
                "message", "Producto actualizado correctamente",
                "data", updated
            )
        );
    }

    @Operation(summary = "Eliminar producto",
            description = "Elimina el producto identificado por id_categoria e id_producto")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Eliminado"),
        @ApiResponse(responseCode = "404", description = "No encontrado")
    })
    @DeleteMapping("/{categoriaId}/{productoId}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Long categoriaId, @PathVariable Long productoId) {
        service.delete(categoriaId, productoId);
        return ResponseEntity.ok(
            Map.of("message", "Producto eliminado correctamente")
        );
    }

    //Reservar y descontar stock para una compra

    @Operation(
            summary = "Reservar y descontar stock para una compra",
            description = "Verifica que haya stock suficiente para todos los productos y descuenta el stock si todo está OK"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Stock añadido correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o stock insuficiente"),
            @ApiResponse(responseCode = "404", description = "Algún producto no existe")
    })
    @PostMapping("/stock/reservar")
    public ResponseEntity<Map<String, Object>> reservarStock(
            @RequestBody List<StockReservaItem> items
    ) {
        service.verificarYDescontarStock(items);
        return ResponseEntity.ok(
                Map.of(
                        "message", "Stock reservado correctamente"
                )
        );
    }

    // Manejo de errores local al controlador
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(EntityNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("message", ex.getMessage() != null ? ex.getMessage() : "Recurso no encontrado"));
    }

    @ExceptionHandler({IllegalArgumentException.class, StockInsuficienteException.class})
    public ResponseEntity<Map<String, Object>> handleBadRequest(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", ex.getMessage() != null ? ex.getMessage() : "Solicitud inválida"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .orElse("Datos inválidos");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", message));
    }
}
