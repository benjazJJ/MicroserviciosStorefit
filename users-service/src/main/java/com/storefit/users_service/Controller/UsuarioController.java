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
import com.storefit.users_service.security.RutUtils;

import java.util.List;
import jakarta.validation.constraints.NotBlank;

@RestController
@RequestMapping("/api/v1/usuarios")
@RequiredArgsConstructor
@Tag(name = "usuarios", description = "Gestión de perfiles de usuario")
public class UsuarioController {

    private final UsuarioService service;

    @GetMapping
    @Operation(summary = "Listar usuarios")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = Usuario.class)))
    })
    public List<Usuario> all(
            @RequestHeader("X-User-Rut") String headerRut, // Header con RUT autenticado
            @RequestHeader("X-User-Rol") String headerRol) { // Header con rol autenticado
        RequestUser user = Authorization.fromHeaders(headerRut, headerRol); // Valida headers
        Authorization.requireAdmin(user); // Solo ADMIN
        return service.findAll();
    }

    @GetMapping("/{rut}")
    @Operation(summary = "Obtener usuario por RUT")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Encontrado", content = @Content(schema = @Schema(implementation = Usuario.class))),
            @ApiResponse(responseCode = "404", description = "No encontrado")
    })
    public Usuario byRut(@PathVariable String rut,
            @RequestHeader("X-User-Rut") String headerRut, // Header con RUT autenticado
            @RequestHeader("X-User-Rol") String headerRol) { // Header con rol autenticado
        RequestUser user = Authorization.fromHeaders(headerRut, headerRol); // Valida headers
        RutUtils.requireDottedOrBadRequest(rut);
        Authorization.requireOwnerOrAdmin(user, rut); // Dueño o ADMIN
        return service.findByRut(rut);
    }

    @GetMapping("/correo/{correo}")
    @Operation(summary = "Obtener usuario por correo")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Encontrado", content = @Content(schema = @Schema(implementation = Usuario.class))),
            @ApiResponse(responseCode = "404", description = "No encontrado")
    })
    public Usuario byCorreo(@PathVariable String correo,
            @RequestHeader("X-User-Rut") String headerRut, // Header con RUT autenticado
            @RequestHeader("X-User-Rol") String headerRol) { // Header con rol autenticado
        Usuario u = service.findByCorreo(correo);
        RequestUser user = Authorization.fromHeaders(headerRut, headerRol); // Valida headers
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
            @ApiResponse(responseCode = "200", description = "Actualizado", content = @Content(schema = @Schema(implementation = Usuario.class))),
            @ApiResponse(responseCode = "404", description = "No encontrado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos")
    })
    public Usuario updateRol(@PathVariable String rut,
            @RequestHeader("X-User-Rut") String headerRut, // Header con RUT autenticado
            @RequestHeader("X-User-Rol") String headerRol, // Header con rol autenticado
            @Valid @RequestBody UpdateRolRequest req) {
        RequestUser user = Authorization.fromHeaders(headerRut, headerRol); // Valida headers
        Authorization.requireAdmin(user); // Solo ADMIN
        service.updateRol(rut, req.getRolId());
        return service.findByRut(rut);
    }

    // DTO para actualización de rol
    public static class UpdateRolRequest {
        @NotNull
        private Long rolId;

        public Long getRolId() {
            return rolId;
        }

        public void setRolId(Long rolId) {
            this.rolId = rolId;
        }
    }

    // Actualizar perfil (dueño o ADMIN)
    @PutMapping("/{rut}/perfil")
    @Operation(summary = "Actualizar perfil de usuario")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Actualizado", content = @Content(schema = @Schema(implementation = Usuario.class))),
            @ApiResponse(responseCode = "404", description = "No encontrado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos")
    })
    public Usuario updatePerfil(@PathVariable String rut,
            @RequestHeader("X-User-Rut") String headerRut,
            @RequestHeader("X-User-Rol") String headerRol,
            @Valid @RequestBody UpdatePerfilRequest req) {
        RequestUser user = Authorization.fromHeaders(headerRut, headerRol);
        RutUtils.requireDottedOrBadRequest(rut);
        Authorization.requireOwnerOrAdmin(user, rut);

        Usuario in = new Usuario();
        in.setRut(rut);
        in.setNombre(req.getNombre());
        in.setApellidos(req.getApellidos());
        in.setCorreo(req.getCorreo());
        in.setTelefono(req.getTelefono());
        in.setDireccion(req.getDireccion());
        in.setFechaNacimiento(req.getFechaNacimiento());
        in.setFotoUri(req.getFotoUri());
        return service.update(rut, in);
    }

    // Actualizar solo foto (dueño o ADMIN)
    @PatchMapping("/{rut}/foto")
    @Operation(summary = "Actualizar solo la foto de perfil")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Actualizada", content = @Content(schema = @Schema(implementation = Usuario.class))),
            @ApiResponse(responseCode = "404", description = "No encontrado")
    })
    public Usuario updateFoto(@PathVariable String rut,
            @RequestHeader("X-User-Rut") String headerRut,
            @RequestHeader("X-User-Rol") String headerRol,
            @Valid @RequestBody UpdateFotoRequest req) {
        RequestUser user = Authorization.fromHeaders(headerRut, headerRol);
        RutUtils.requireDottedOrBadRequest(rut);
        Authorization.requireOwnerOrAdmin(user, rut);
        return service.updateFoto(rut, req.getFotoUri());
    }

    // Chequeos de unicidad para REGISTRO (públicos)
    @GetMapping("/check/rut/{rut}")
    @Operation(summary = "Chequear disponibilidad de RUT para registro")
    public CheckResponse checkRut(@PathVariable String rut) {
        RutUtils.requireDottedOrBadRequest(rut);
        boolean available = !service.existsByRut(rut);
        return new CheckResponse(available);
    }

    @GetMapping("/check/correo/{correo}")
    @Operation(summary = "Chequear disponibilidad de correo para registro")
    public CheckResponse checkCorreo(@PathVariable String correo) {
        boolean available = !service.findByCorreoOptional(correo).isPresent();
        return new CheckResponse(available);
    }

    @GetMapping("/check/telefono/{telefono}")
    @Operation(summary = "Chequear disponibilidad de teléfono para registro")
    public CheckResponse checkTelefono(@PathVariable String telefono) {
        boolean available = !service.existsByTelefono(telefono);
        return new CheckResponse(available);
    }

    // Chequeos de disponibilidad para EDITAR PERFIL (dueño o ADMIN)
    @GetMapping("/check-actualizar/correo")
    @Operation(summary = "Chequear correo disponible para actualizar perfil")
    public CheckResponse checkActualizarCorreo(@RequestParam String rut,
            @RequestParam String correo,
            @RequestHeader("X-User-Rut") String headerRut,
            @RequestHeader("X-User-Rol") String headerRol) {
        RequestUser user = Authorization.fromHeaders(headerRut, headerRol);
        RutUtils.requireDottedOrBadRequest(rut);
        Authorization.requireOwnerOrAdmin(user, rut);
        var other = service.findByCorreoOptional(correo);
        boolean available = other.isEmpty() || other.get().getRut().equalsIgnoreCase(rut);
        return new CheckResponse(available);
    }

    @GetMapping("/check-actualizar/telefono")
    @Operation(summary = "Chequear teléfono disponible para actualizar perfil")
    public CheckResponse checkActualizarTelefono(@RequestParam String rut,
            @RequestParam String telefono,
            @RequestHeader("X-User-Rut") String headerRut,
            @RequestHeader("X-User-Rol") String headerRol) {
        RequestUser user = Authorization.fromHeaders(headerRut, headerRol);
        RutUtils.requireDottedOrBadRequest(rut);
        Authorization.requireOwnerOrAdmin(user, rut);
        boolean available = service.isTelefonoDisponibleParaActualizar(rut, telefono);
        return new CheckResponse(available);
    }

    // DTOs para update perfil y foto
    @lombok.Data
    public static class UpdatePerfilRequest {
        @NotNull
        @NotBlank
        private String nombre;
        @NotNull
        @NotBlank
        private String apellidos;
        @NotNull
        @NotBlank
        @jakarta.validation.constraints.Email
        private String correo;
        private String telefono;
        private String direccion;
        private String fechaNacimiento;
        private String fotoUri;
    }

    @lombok.Data
    public static class UpdateFotoRequest {
        @NotNull
        @NotBlank
        private String fotoUri;
    }

    public record CheckResponse(boolean available) {
    }
}
