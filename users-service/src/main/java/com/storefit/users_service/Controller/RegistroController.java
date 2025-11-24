package com.storefit.users_service.Controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.storefit.users_service.Model.Registro;
import com.storefit.users_service.Model.Usuario;
import com.storefit.users_service.Service.RegistroService;
import com.storefit.users_service.Service.UsuarioService;
import com.storefit.users_service.security.Authorization;
import com.storefit.users_service.security.RequestUser;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/registros")
@Tag(name = "registros", description = "Registro de credenciales y login")
@RequiredArgsConstructor
public class RegistroController {

    private final RegistroService service;
    private final UsuarioService usuarioService;

    @GetMapping("/by-usuario/{usuario}")
    @Operation(summary = "Obtener registro por usuario (correo)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Encontrado", content = @Content(schema = @Schema(implementation = Registro.class))),
            @ApiResponse(responseCode = "404", description = "No encontrado")
    })
    public Registro byUsuario(@PathVariable String usuario,
            @RequestHeader("X-User-Rut") String headerRut, // Header con RUT autenticado
            @RequestHeader("X-User-Rol") String headerRol) { // Header con rol autenticado
        RequestUser user = Authorization.fromHeaders(headerRut, headerRol); // Valida headers
        Authorization.requireAdmin(user); // Solo ADMIN puede ver registros
        return service.findByUsuario(usuario);
    }

    @PostMapping("/login")
    @Operation(summary = "Login por correo + contraseña", description = "Valida credenciales y devuelve datos de rol y perfil mínimo.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Autenticado", content = @Content(schema = @Schema(implementation = LoginResponse.class))),
            @ApiResponse(responseCode = "401", description = "Credenciales inválidas")
    })
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        try {
            var reg = service.autenticarYObtener(req.getCorreo(), req.getContrasenia());
            var u = usuarioService.findByRut(reg.getRut());
            return ResponseEntity.ok(
                    new LoginResponse(true, reg.getUsuario(), u.getRut(), u.getNombre(), u.getCorreo(), reg.getRolId(),
                            reg.getRolNombre()));
        } catch (org.springframework.web.server.ResponseStatusException e) {
            if (e.getStatusCode().value() == 401) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new LoginResponse(false, null, null, null, null, null, null));
            }
            throw e;
        }
    }

    // Cambio de contraseña
    @PostMapping("/cambiar-contrasenia")
    @Operation(summary = "Cambiar contraseña", description = "Valida credenciales actuales y actualiza a una nueva contraseña")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Actualizada", content = @Content(schema = @Schema(implementation = ChangePasswordResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "401", description = "Credenciales inválidas"),
            @ApiResponse(responseCode = "404", description = "Usuario no existe")
    })
    public ResponseEntity<ChangePasswordResponse> cambiarContrasenia(@Valid @RequestBody ChangePasswordRequest req) {
        if (!req.getNuevaContrasenia().equals(req.getConfirmarContrasenia())) {
            return ResponseEntity.badRequest()
                    .body(new ChangePasswordResponse(false, "Las contraseñas no coinciden"));
        }
        try {
            service.cambiarContrasenia(req.getUsuarioOCorreo(), req.getContraseniaActual(), req.getNuevaContrasenia());
            return ResponseEntity.ok(new ChangePasswordResponse(true, "Contraseña actualizada correctamente"));
        } catch (org.springframework.web.server.ResponseStatusException e) {
            if (e.getStatusCode().value() == 401) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ChangePasswordResponse(false, "La contrase�a actual no es correcta"));
            }
            throw e;
        }
    }

    // Registro completo: crea Usuario (perfil) + Registro (credenciales)
    @PostMapping("/registro-completo")
    @ResponseStatus(HttpStatus.CREATED)
    @org.springframework.transaction.annotation.Transactional
    @Operation(summary = "Registro completo", description = "Crea perfil Usuario + Registro credenciales")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Creado", content = @Content(schema = @Schema(implementation = Registro.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos")
    })
    public RegistroCompletoResponse registroCompleto(@Valid @RequestBody RegistroCompletoRequest req) {
        if (!req.getContrasenia().equals(req.getConfirmarContrasenia())) {
            throw new IllegalArgumentException("Las contraseñas no coinciden");
        }

        // Crear perfil de usuario
        Usuario u = new Usuario();
        // Enforce and persist dotted RUT
        com.storefit.users_service.security.RutUtils.requireDottedOrBadRequest(req.getRut());
        u.setRut(req.getRut());
        u.setNombre(req.getNombre());
        u.setApellidos(req.getApellidos());
        u.setCorreo(req.getCorreo());
        u.setTelefono(req.getTelefono());
        u.setDireccion(req.getDireccion());
        u.setFechaNacimiento(req.getFechaNacimiento());
        usuarioService.create(u);

        // Crear credenciales. Usamos el correo como nombre de usuario de acceso
        Registro reg = new Registro();
        reg.setRut(req.getRut());
        reg.setUsuario(req.getCorreo());
        reg.setContrasenia(req.getContrasenia());
        reg.setAddress(req.getDireccion());
        // rol por defecto CLIENTE=1 (se asigna en RegistroService si es null)
        service.create(reg);

        return new RegistroCompletoResponse(true, reg.getUsuario());
    }

    // DTOs para login (mantenemos mismos nombres que Android)
    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class LoginRequest {
        @NotBlank
        @Schema(description = "Correo del usuario", example = "admin@test.com")
        @Email
        private String correo;
        @NotBlank
        @Schema(description = "Contraseña", example = "Admin123!")
        private String contrasenia;

        public String getCorreo() {
            return correo;
        }

        public void setCorreo(String correo) {
            this.correo = correo;
        }

        public String getContrasenia() {
            return contrasenia;
        }

        public void setContrasenia(String contrasenia) {
            this.contrasenia = contrasenia;
        }
    }

    public record LoginResponse(
            boolean success,
            String usuario,
            String rut,
            String nombre,
            String correo,
            Long rolId,
            String rolNombre) {
    }

    // DTO para cambio de contraseña
    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class ChangePasswordRequest {
        @NotBlank
        @Schema(description = "Usuario o correo", example = "juan@example.com")
        private String usuarioOCorreo;
        @NotBlank
        @Schema(description = "Contraseña actual", example = "ClaveSegura123")
        private String contraseniaActual;
        @NotBlank
        @Schema(description = "Nueva contraseña", example = "NuevaClave123")
        private String nuevaContrasenia;
        @NotBlank
        @Schema(description = "Confirmación de nueva contraseña", example = "NuevaClave123")
        private String confirmarContrasenia;
    }

    public record ChangePasswordResponse(boolean success, String message) {
    }

    // DTO para registro completo
    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class RegistroCompletoRequest {
        @NotBlank
        @Schema(description = "RUT", example = "12345678-9")
        private String rut;
        @NotBlank
        @Schema(description = "Nombre", example = "Juan")
        private String nombre;
        @NotBlank
        @Schema(description = "Apellidos", example = "Pérez López")
        private String apellidos; // requerido por el modelo
        @NotBlank
        @Schema(description = "Correo", example = "juan@example.com")
        @Email
        private String correo;
        @NotBlank
        @Schema(description = "Fecha de nacimiento (yyyy-mm-dd)", example = "1995-10-10")
        private String fechaNacimiento; // yyyy-mm-dd
        @NotBlank
        @Schema(description = "Contraseña", example = "ClaveSegura123")
        private String contrasenia;
        @NotBlank
        @Schema(description = "Confirmación de contraseña", example = "ClaveSegura123")
        private String confirmarContrasenia;
        @NotBlank
        @Schema(description = "Dirección", example = "Calle Falsa 123")
        private String direccion;
        @NotBlank
        @Schema(description = "Teléfono", example = "987654321")
        private String telefono;
    }

    public record RegistroCompletoResponse(boolean success, String usuario) {
    }
}


