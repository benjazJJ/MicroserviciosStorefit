package com.storefit.users_service.Controller;

import com.storefit.users_service.Model.Rol;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.storefit.users_service.Service.RolService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestHeader;

import com.storefit.users_service.security.Authorization;
import com.storefit.users_service.security.RequestUser;

import java.util.List;

@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
@Tag(name = "roles", description = "Consulta de roles disponibles")
public class RolController {

    private final RolService service;

    @GetMapping
    @Operation(summary = "Listar roles")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = Rol.class)))
    })
    public List<Rol> all(
            @RequestHeader("X-User-Rut") String headerRut,   // Header con RUT autenticado
            @RequestHeader("X-User-Rol") String headerRol) { // Header con rol autenticado
        RequestUser user = Authorization.fromHeaders(headerRut, headerRol); // Valida headers
        Authorization.requireAdmin(user); // Solo ADMIN
        return service.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener rol por ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Encontrado",
            content = @Content(schema = @Schema(implementation = Rol.class))),
        @ApiResponse(responseCode = "404", description = "No encontrado")
    })
    public Rol byId(@PathVariable Long id,
                    @RequestHeader("X-User-Rut") String headerRut,   // Header con RUT autenticado
                    @RequestHeader("X-User-Rol") String headerRol) { // Header con rol autenticado
        RequestUser user = Authorization.fromHeaders(headerRut, headerRol); // Valida headers
        Authorization.requireAdmin(user); // Solo ADMIN
        return service.findById(id);
    }
}
