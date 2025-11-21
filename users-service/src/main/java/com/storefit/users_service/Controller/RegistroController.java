package com.storefit.users_service.Controller;

import com.storefit.users_service.Model.Registro;
import com.storefit.users_service.Model.Usuario;
import com.storefit.users_service.Service.RegistroService;
import com.storefit.users_service.Service.UsuarioService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import lombok.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestHeader;

import com.storefit.users_service.security.Authorization;
import com.storefit.users_service.security.RequestUser;

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
        @ApiResponse(responseCode = "200", description = "Encontrado",
            content = @Content(schema = @Schema(implementation = Registro.class))),
        @ApiResponse(responseCode = "404", description = "No encontrado")
    })
    public Registro byUsuario(@PathVariable String usuario,
                              @RequestHeader("X-User-Rut") String headerRut,   // Header con RUT autenticado
                              @RequestHeader("X-User-Rol") String headerRol) { // Header con rol autenticado
        RequestUser user = Authorization.fromHeaders(headerRut, headerRol); // Valida headers
        Authorization.requireAdmin(user); // Solo ADMIN puede ver registros
        return service.findByUsuario(usuario);
    }

    @PostMapping("/login")
    @Operation(summary = "Login por correo + contraseña", description = "Valida credenciales y devuelve datos de rol y perfil mínimo.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Autenticado",
            content = @Content(schema = @Schema(implementation = LoginResponse.class))),
        @ApiResponse(responseCode = "400", description = "Credenciales inválidas")
    })
    public LoginResponse login(@Valid @RequestBody LoginRequest req) {
        var reg = service.autenticarYObtener(req.getCorreo(), req.getContrasenia());
        var u = usuarioService.findByRut(reg.getRut());
        return new LoginResponse(true, reg.getUsuario(), u.getRut(), u.getNombre(), u.getCorreo(), reg.getRolId(), reg.getRolNombre());
    }

    // Registro completo: crea Usuario (perfil) + Registro (credenciales)
    @PostMapping("/registro-completo")
    @ResponseStatus(HttpStatus.CREATED)
    @org.springframework.transaction.annotation.Transactional
    @Operation(summary = "Registro completo", description = "Crea perfil Usuario + Registro credenciales")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Creado",
            content = @Content(schema = @Schema(implementation = Registro.class))),
        @ApiResponse(responseCode = "400", description = "Datos inválidos")
    })
    public RegistroCompletoResponse registroCompleto(@Valid @RequestBody RegistroCompletoRequest req) {
        if (!req.getContrasenia().equals(req.getConfirmarContrasenia())) {
            throw new IllegalArgumentException("Las contraseñas no coinciden");
        }

        // Crear perfil de usuario
        Usuario u = new Usuario();
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
        public String getCorreo() { return correo; }
        public void setCorreo(String correo) { this.correo = correo; }
        public String getContrasenia() { return contrasenia; }
        public void setContrasenia(String contrasenia) { this.contrasenia = contrasenia; }
    }
    public record LoginResponse(
            boolean success,
            String usuario,
            String rut,
            String nombre,
            String correo,
            Long rolId,
            String rolNombre
    ) {}

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

    public record RegistroCompletoResponse(boolean success, String usuario) {}
}
