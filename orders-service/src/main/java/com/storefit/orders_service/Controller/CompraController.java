package com.storefit.orders_service.Controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.storefit.orders_service.Model.Compra;
import com.storefit.orders_service.Service.CompraService;
import com.storefit.orders_service.security.Authorization;
import com.storefit.orders_service.security.RequestUser;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/compras")
@RequiredArgsConstructor
@Tag(name = "compras", description = "Gestión de compras de usuarios")
public class CompraController {

    private final CompraService service;

    @GetMapping
    @Operation(summary = "Listar compras", description = "Obtiene todas las compras registradas")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = Compra.class)))
    })
    public List<Compra> all(
            @RequestHeader("X-User-Rut") String headerRut,
            @RequestHeader("X-User-Rol") String headerRol) {
        RequestUser user = Authorization.fromHeaders(headerRut, headerRol);
        Authorization.requireAdmin(user);
        return service.listarTodas();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener compra por ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Encontrada",
            content = @Content(schema = @Schema(implementation = Compra.class))),
        @ApiResponse(responseCode = "404", description = "No encontrada")
    })
    public ResponseEntity<Compra> byId(@PathVariable Long id,
            @RequestHeader("X-User-Rut") String headerRut,
            @RequestHeader("X-User-Rol") String headerRol) {
        var compra = service.obtenerPorId(id);
        RequestUser user = Authorization.fromHeaders(headerRut, headerRol);
        if (!(user.isAdmin() || user.getRut().equalsIgnoreCase(compra.getRutUsuario()))) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN,
                    "No autorizado para ver esta compra");
        }
        return ResponseEntity.ok(compra);
    }

    @GetMapping("/usuario/{rut}")
    @Operation(summary = "Historial por RUT", description = "Compras de un usuario ordenadas por fecha desc")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = Compra.class)))
    })
    public List<Compra> byRut(@PathVariable String rut,
            @RequestHeader("X-User-Rut") String headerRut,
            @RequestHeader("X-User-Rol") String headerRol) {
        RequestUser user = Authorization.fromHeaders(headerRut, headerRol);
        Authorization.requireOwnerOrAdmin(user, rut);
        return service.historialPorRut(rut);
    }

    @GetMapping("/usuario/{rut}/total")
    @Operation(summary = "Total gastado por RUT")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = Integer.class)))
    })
    public Integer totalPorRut(@PathVariable String rut,
            @RequestHeader("X-User-Rut") String headerRut,
            @RequestHeader("X-User-Rol") String headerRol) {
        RequestUser user = Authorization.fromHeaders(headerRut, headerRol);
        Authorization.requireOwnerOrAdmin(user, rut);
        return service.totalGastado(rut);
    }

    @PostMapping
    @Operation(summary = "Crear compra", description = "Valida usuario y stock en catálogo, luego persiste")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Creada",
            content = @Content(schema = @Schema(implementation = Compra.class))),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "404", description = "Usuario o producto no encontrado"),
        @ApiResponse(responseCode = "503", description = "Servicios externos no disponibles")
    })
    public ResponseEntity<Compra> create(
            @RequestHeader("X-User-Rut") String headerRut,
            @RequestHeader("X-User-Rol") String headerRol,
            @RequestBody @Valid Compra compra) {
        RequestUser user = Authorization.fromHeaders(headerRut, headerRol);
        Authorization.requireCliente(user);
        if (!user.getRut().equalsIgnoreCase(compra.getRutUsuario())) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN,
                    "No puedes crear compras para otro usuario");
        }
        Compra creada = service.crearCompra(compra);
        return ResponseEntity
                .created(URI.create("/api/v1/compras/" + creada.getIdCompra()))
                .body(creada);
    }
}
