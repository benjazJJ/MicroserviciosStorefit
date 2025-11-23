package com.storefit.users_service.Service;

import com.storefit.users_service.Model.Registro;
import com.storefit.users_service.Repository.RegistroRepository;
import com.storefit.users_service.Repository.UsuarioRepository;
import com.storefit.users_service.Repository.RolRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class RegistroService {

    private final RegistroRepository repo;
    private final UsuarioRepository usuarioRepo;
    private final RolRepository rolRepo;
    private final PasswordEncoder passwordEncoder = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();

    public Registro create(Registro r) {
        if (r.getRolId() == null) {
            r.setRolId(1L); // Rol CLIENTE por defecto
        }
        var rol = rolRepo.findById(r.getRolId())
                .orElseThrow(() -> new EntityNotFoundException("Rol no encontrado: " + r.getRolId()));
        r.setRolNombre(rol.getNombreRol());
        if (repo.existsByUsuarioIgnoreCase(r.getUsuario())) {
            throw new IllegalArgumentException("Usuario ya registrado: " + r.getUsuario());
        }
        r.setContrasenia(passwordEncoder.encode(r.getContrasenia()));
        return repo.save(r);
    }

    public Registro findByUsuario(String usuario) {
        return repo.findByUsuarioIgnoreCase(usuario)
                .orElseThrow(() -> new EntityNotFoundException("Registro no encontrado para usuario: " + usuario));
    }

    public boolean validarLogin(String usuarioOCorreo, String contrasenia) {
        var reg = resolveRegistro(usuarioOCorreo);
        if (!passwordEncoder.matches(contrasenia, reg.getContrasenia())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas");
        }
        return true;
    }

    public Registro autenticarYObtener(String usuarioOCorreo, String contrasenia) {
        var reg = resolveRegistro(usuarioOCorreo);
        if (!passwordEncoder.matches(contrasenia, reg.getContrasenia())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas");
        }
        return reg;
    }

    public void cambiarContrasenia(String usuarioOCorreo, String contraseniaActual, String nuevaContrasenia) {
        if (usuarioOCorreo == null || usuarioOCorreo.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debe indicar usuario o correo");
        }
        if (contraseniaActual == null || contraseniaActual.isBlank() || nuevaContrasenia == null
                || nuevaContrasenia.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Contraseñas requeridas");
        }

        var reg = resolveRegistroOr404(usuarioOCorreo);

        if (!passwordEncoder.matches(contraseniaActual, reg.getContrasenia())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales invalidas"); // capturado en el controller
        }

        reg.setContrasenia(passwordEncoder.encode(nuevaContrasenia));
        repo.save(reg);
    }

    private Registro resolveRegistro(String usuarioOCorreo) {
        var regOpt = repo.findByUsuarioIgnoreCase(usuarioOCorreo);
        if (regOpt.isEmpty()) {
            var userOpt = usuarioRepo.findByCorreoIgnoreCase(usuarioOCorreo);
            if (userOpt.isPresent()) {
                regOpt = repo.findByRut(userOpt.get().getRut());
            }
        }
        return regOpt.orElseThrow(() -> new EntityNotFoundException("Usuario no existe: " + usuarioOCorreo));
    }

    private Registro resolveRegistroOr404(String usuarioOCorreo) {
        var regOpt = repo.findByUsuarioIgnoreCase(usuarioOCorreo);
        if (regOpt.isEmpty()) {
            var userOpt = usuarioRepo.findByCorreoIgnoreCase(usuarioOCorreo);
            if (userOpt.isPresent()) {
                regOpt = repo.findByRut(userOpt.get().getRut());
            }
        }
        return regOpt.orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no existe: " + usuarioOCorreo));
    }
}
