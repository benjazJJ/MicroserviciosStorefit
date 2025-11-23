package com.storefit.users_service.Controller;

import com.storefit.users_service.Model.Registro;
import com.storefit.users_service.Model.Usuario;
import com.storefit.users_service.Repository.RegistroRepository;
import com.storefit.users_service.Service.RegistroService;
import com.storefit.users_service.Service.UsuarioService;
import com.storefit.users_service.security.Authorization;
import com.storefit.users_service.security.RequestUser;
import com.storefit.users_service.security.RutUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/admin/usuarios")
@RequiredArgsConstructor
@Tag(name = "Admin Usuarios", description = "Endpoints de administración de usuarios + roles")
public class AdminUsuarioController {

    private final UsuarioService usuarioService;
    private final RegistroService registroService;
    private final RegistroRepository registroRepository;

    private void requireAdmin(String rut, String rol) {
        RequestUser user = Authorization.fromHeaders(rut, rol);
        Authorization.requireAdmin(user);
    }

    @GetMapping
    @Operation(summary = "Listar usuarios con rol")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = UsuarioConRolDTO.class)))
    })
    public List<UsuarioConRolDTO> listar(
            @RequestHeader("X-User-Rut") String headerRut,
            @RequestHeader("X-User-Rol") String headerRol) {
        requireAdmin(headerRut, headerRol);
        return usuarioService.findAll().stream()
                .map(u -> toDto(u, registroRepository.findByRut(u.getRut()).orElse(null)))
                .collect(Collectors.toList());
    }

    @GetMapping("/{rut}")
    @Operation(summary = "Detalle de usuario con rol")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = UsuarioConRolDTO.class))),
            @ApiResponse(responseCode = "404", description = "No encontrado")
    })
    public UsuarioConRolDTO detalle(@PathVariable String rut,
                                    @RequestHeader("X-User-Rut") String headerRut,
                                    @RequestHeader("X-User-Rol") String headerRol) {
        requireAdmin(headerRut, headerRol);
        RutUtils.requireDottedOrBadRequest(rut);
        Usuario u = usuarioService.findByRut(rut);
        Registro r = registroRepository.findByRut(rut).orElse(null);
        return toDto(u, r);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear usuario + registro + rol")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Creado",
                    content = @Content(schema = @Schema(implementation = UsuarioConRolDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos")
    })
    public UsuarioConRolDTO crear(
            @RequestHeader("X-User-Rut") String headerRut,
            @RequestHeader("X-User-Rol") String headerRol,
            @Valid @RequestBody AdminCrearUsuarioRequest req) {
        requireAdmin(headerRut, headerRol);

        String rut = RutUtils.requireDottedOrBadRequest(req.getRut());

        Usuario u = new Usuario();
        u.setRut(rut);
        u.setNombre(req.getNombre());
        u.setApellidos(req.getApellidos());
        u.setCorreo(req.getCorreo());
        u.setTelefono(req.getTelefono());
        u.setDireccion(req.getDireccion());
        u.setFechaNacimiento(req.getFechaNacimiento());
        u.setFotoUri(req.getFotoUri());
        u = usuarioService.create(u);

        Registro r = new Registro();
        r.setRut(rut);
        r.setUsuario(req.getCorreo());
        r.setContrasenia(req.getContrasenia());
        r.setAddress(req.getDireccion());
        r.setRolId(req.getRolId());
        r = registroService.create(r);

        return toDto(u, r);
    }

    @PutMapping("/{rut}")
    @Operation(summary = "Actualizar datos de usuario (sync correo en registro si cambia)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Actualizado",
                    content = @Content(schema = @Schema(implementation = UsuarioConRolDTO.class))),
            @ApiResponse(responseCode = "404", description = "No encontrado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos")
    })
    public UsuarioConRolDTO actualizar(@PathVariable String rut,
                                       @RequestHeader("X-User-Rut") String headerRut,
                                       @RequestHeader("X-User-Rol") String headerRol,
                                       @Valid @RequestBody AdminActualizarUsuarioRequest req) {
        requireAdmin(headerRut, headerRol);
        RutUtils.requireDottedOrBadRequest(rut);

        // Obtener correo previo para decidir sync
        Usuario before = usuarioService.findByRut(rut);

        Usuario in = new Usuario();
        in.setRut(rut);
        in.setNombre(req.getNombre());
        in.setApellidos(req.getApellidos());
        in.setCorreo(req.getCorreo());
        in.setTelefono(req.getTelefono());
        in.setDireccion(req.getDireccion());
        in.setFechaNacimiento(req.getFechaNacimiento());
        in.setFotoUri(req.getFotoUri());
        Usuario updated = usuarioService.update(rut, in);

        if (req.getCorreo() != null && !req.getCorreo().equalsIgnoreCase(before.getCorreo())) {
            var regOpt = registroRepository.findByRut(rut);
            if (regOpt.isPresent()) {
                var reg = regOpt.get();
                reg.setUsuario(updated.getCorreo());
                registroRepository.save(reg);
            }
        }

        Registro reg = registroRepository.findByRut(rut).orElse(null);
        return toDto(updated, reg);
    }

    private static UsuarioConRolDTO toDto(Usuario u, Registro r) {
        UsuarioConRolDTO dto = new UsuarioConRolDTO();
        dto.setRut(u.getRut());
        dto.setNombre(u.getNombre());
        dto.setApellidos(u.getApellidos());
        dto.setCorreo(u.getCorreo());
        dto.setTelefono(u.getTelefono());
        dto.setDireccion(u.getDireccion());
        dto.setFechaNacimiento(u.getFechaNacimiento());
        dto.setFotoUri(u.getFotoUri());
        if (r != null) {
            dto.setRolId(r.getRolId());
            dto.setRolNombre(r.getRolNombre());
        }
        return dto;
    }

    // DTOs
    @Data
    public static class UsuarioConRolDTO {
        private String rut;
        private String nombre;
        private String apellidos;
        private String correo;
        private String telefono;
        private String direccion;
        private String fechaNacimiento;
        private String fotoUri;
        private Long rolId;
        private String rolNombre;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AdminCrearUsuarioRequest {
        @NotBlank private String rut;
        @NotBlank private String nombre;
        @NotBlank private String apellidos;
        @NotBlank @Email private String correo;
        private String telefono;
        private String direccion;
        private String fechaNacimiento;
        private String fotoUri;
        @NotBlank private String contrasenia;
        @NotNull private Long rolId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AdminActualizarUsuarioRequest {
        @NotBlank private String nombre;
        @NotBlank private String apellidos;
        @NotBlank @Email private String correo;
        private String telefono;
        private String direccion;
        private String fechaNacimiento;
        private String fotoUri;
    }
}
