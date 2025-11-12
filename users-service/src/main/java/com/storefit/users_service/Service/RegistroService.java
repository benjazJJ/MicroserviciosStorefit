package com.storefit.users_service.Service;

import com.storefit.users_service.Model.Registro;
import com.storefit.users_service.Repository.RegistroRepository;
import com.storefit.users_service.Repository.UsuarioRepository;
import com.storefit.users_service.Repository.RolRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class RegistroService {

    private final RegistroRepository repo;
    private final UsuarioRepository usuarioRepo;
    private final RolRepository rolRepo;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public Registro create(Registro r) {
        if (r.getRolId() == null) {
            r.setRolId(1L); // Rol CLIENTE por defecto
        }
        // Asignar nombre de rol coherente con el ID
        var rol = rolRepo.findById(r.getRolId())
                .orElseThrow(() -> new EntityNotFoundException("Rol no encontrado: " + r.getRolId()));
        r.setRolNombre(rol.getNombreRol());
        if (repo.existsByUsuarioIgnoreCase(r.getUsuario()))
            throw new IllegalArgumentException("Usuario ya registrado: " + r.getUsuario());
        r.setContrasenia(passwordEncoder.encode(r.getContrasenia()));
        return repo.save(r);
    }

    public Registro findByUsuario(String usuario) {
        return repo.findByUsuarioIgnoreCase(usuario)
                .orElseThrow(() -> new EntityNotFoundException("Registro no encontrado para usuario: " + usuario));
    }

    public boolean validarLogin(String usuarioOCorreo, String contrasenia) {
        var regOpt = repo.findByUsuarioIgnoreCase(usuarioOCorreo);
        if (regOpt.isEmpty()) {
            var userOpt = usuarioRepo.findByCorreoIgnoreCase(usuarioOCorreo);
            if (userOpt.isPresent()) {
                regOpt = repo.findByRut(userOpt.get().getRut());
            }
        }
        Registro reg = regOpt.orElseThrow(() -> new EntityNotFoundException("Usuario no existe: " + usuarioOCorreo));
        return passwordEncoder.matches(contrasenia, reg.getContrasenia());
    }

    public Registro autenticarYObtener(String usuarioOCorreo, String contrasenia) {
        var regOpt = repo.findByUsuarioIgnoreCase(usuarioOCorreo);
        if (regOpt.isEmpty()) {
            var userOpt = usuarioRepo.findByCorreoIgnoreCase(usuarioOCorreo);
            if (userOpt.isPresent()) {
                regOpt = repo.findByRut(userOpt.get().getRut());
            }
        }
        Registro reg = regOpt.orElseThrow(() -> new EntityNotFoundException("Usuario no existe: " + usuarioOCorreo));
        if (!passwordEncoder.matches(contrasenia, reg.getContrasenia())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas");
        }
        return reg;
    }
}
