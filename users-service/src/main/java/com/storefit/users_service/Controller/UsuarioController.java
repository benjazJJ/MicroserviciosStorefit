package com.storefit.users_service.Controller;

import com.storefit.users_service.Model.Usuario;
import com.storefit.users_service.Service.UsuarioService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService service;

    @GetMapping
    public List<Usuario> all() { return service.findAll(); }

    @GetMapping("/{rut}")
    public Usuario byRut(@PathVariable String rut) { return service.findByRut(rut); }

    @GetMapping("/correo/{correo}")
    public Usuario byCorreo(@PathVariable String correo) { return service.findByCorreo(correo); }

    // Solo PUT para actualizar el rol del usuario existente
    @PutMapping("/{rut}")
    public Usuario updateRol(@PathVariable String rut, @Valid @RequestBody UpdateRolRequest req) {
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
