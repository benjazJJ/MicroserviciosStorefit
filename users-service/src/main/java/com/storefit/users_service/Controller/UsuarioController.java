package com.storefit.users_service.Controller;

import com.storefit.users_service.Model.Usuario;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.storefit.users_service.Service.UsuarioService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestHeader;

import com.storefit.users_service.security.Authorization;
import com.storefit.users_service.security.RequestUser;

import java.util.List;

@RestController
@RequestMapping("/api/v1/usuarios")
@RequiredArgsConstructor
@Tag(name = "usuarios", description = "Gestión de perfiles de usuario")
public class UsuarioController {

    private final UsuarioService service;

    @GetMapping
    @Operation(summary = "Listar usuarios")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = Usuario.class)))
    })
    public List<Usuario> all(
            @RequestHeader("X-User-Rut") String headerRut,
            @RequestHeader("X-User-Rol") String headerRol) {
        RequestUser user = Authorization.fromHeaders(headerRut, headerRol);
        Authorization.requireAdmin(user);
        return service.findAll();
    }

    @GetMapping("/{rut}")
    @Operation(summary = "Obtener usuario por RUT")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Encontrado",
            content = @Content(schema = @Schema(implementation = Usuario.class))),
        @ApiResponse(responseCode = "404", description = "No encontrado")
    })
    public Usuario byRut(@PathVariable String rut,
                         @RequestHeader("X-User-Rut") String headerRut,
                         @RequestHeader("X-User-Rol") String headerRol) {
        RequestUser user = Authorization.fromHeaders(headerRut, headerRol);
        Authorization.requireOwnerOrAdmin(user, rut);
        return service.findByRut(rut);
    }

    @GetMapping("/correo/{correo}")
    @Operation(summary = "Obtener usuario por correo")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Encontrado",
            content = @Content(schema = @Schema(implementation = Usuario.class))),
        @ApiResponse(responseCode = "404", description = "No encontrado")
    })
    public Usuario byCorreo(@PathVariable String correo,
                            @RequestHeader("X-User-Rut") String headerRut,
                            @RequestHeader("X-User-Rol") String headerRol) {
        Usuario u = service.findByCorreo(correo);
        RequestUser user = Authorization.fromHeaders(headerRut, headerRol);
        if (!(user.isAdmin() || (u.getRut() != null && u.getRut().equalsIgnoreCase(user.getRut())))) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN,
                    "No autorizado a ver este usuario");
        }
        return u;
    }

    // Solo PUT para actualizar el rol del usuario existente
    @PutMapping("/{rut}")
    @Operation(summary = "Actualizar rol por RUT")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Actualizado",
            content = @Content(schema = @Schema(implementation = Usuario.class))),
        @ApiResponse(responseCode = "404", description = "No encontrado"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos")
    })
    public Usuario updateRol(@PathVariable String rut,
                             @RequestHeader("X-User-Rut") String headerRut,
                             @RequestHeader("X-User-Rol") String headerRol,
                             @Valid @RequestBody UpdateRolRequest req) {
        RequestUser user = Authorization.fromHeaders(headerRut, headerRol);
        Authorization.requireAdmin(user);
        service.updateRol(rut, req.getRolId());
        return service.findByRut(rut);
    }

    // DTO para actualización de rol
    public static class UpdateRolRequest {
        @NotNull private Long rolId;
        public Long getRolId() { return rolId; }
        public void setRolId(Long rolId) { this.rolId = rolId; }
    }
}
