package com.storefit.users_service.Service;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import jakarta.persistence.EntityNotFoundException;

import com.storefit.users_service.Model.Usuario;
import com.storefit.users_service.Repository.UsuarioRepository;
import com.storefit.users_service.Repository.RegistroRepository;
import com.storefit.users_service.Repository.RolRepository;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository repo;
    private final RegistroRepository registroRepo;
    private final RolRepository rolRepo;

    public List<Usuario> findAll() { return repo.findAll(); }

    public Usuario findByRut(String rut) {
        return repo.findById(rut).orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado: " + rut));
    }

    public Usuario findByCorreo(String correo) {
        return repo.findByCorreoIgnoreCase(correo)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado por correo: " + correo));
    }

    @Transactional
    public Usuario create(Usuario u) {
        if (repo.existsById(u.getRut()))
            throw new IllegalArgumentException("Ya existe un usuario con rut " + u.getRut());
        if (u.getCorreo() != null && repo.existsByCorreoIgnoreCase(u.getCorreo()))
            throw new IllegalArgumentException("Ya existe un usuario con correo " + u.getCorreo());
        if (u.getTelefono() != null && !u.getTelefono().isBlank() && repo.existsByTelefono(u.getTelefono()))
            throw new IllegalArgumentException("Ya existe un usuario con telefono " + u.getTelefono());
        return repo.save(u);
    }

    @Transactional
    public Usuario update(String rut, Usuario in) {
        Usuario db = findByRut(rut);

        if (in.getCorreo() != null && !in.getCorreo().equalsIgnoreCase(db.getCorreo())
                && repo.existsByCorreoIgnoreCase(in.getCorreo()))
            throw new IllegalArgumentException("Correo ya utilizado: " + in.getCorreo());

        if (in.getTelefono() != null && !in.getTelefono().equals(db.getTelefono())
                && repo.existsByTelefono(in.getTelefono()))
            throw new IllegalArgumentException("Telefono ya utilizado: " + in.getTelefono());

        db.setNombre(in.getNombre());
        db.setApellidos(in.getApellidos());
        db.setCorreo(in.getCorreo());
        db.setTelefono(in.getTelefono());
        db.setDireccion(in.getDireccion());
        db.setFechaNacimiento(in.getFechaNacimiento());
        db.setFotoUri(in.getFotoUri());
        return repo.save(db);
    }

    public void delete(String rut) { repo.delete(findByRut(rut)); }

    @Transactional
    public void updateRol(String rut, Long rolId) {
        var reg = registroRepo.findByRut(rut)
                .orElseThrow(() -> new EntityNotFoundException("Registro no encontrado para rut: " + rut));
        reg.setRolId(rolId);
        // Actualiza el nombre del rol para consistencia
        var rol = rolRepo.findById(rolId).orElseThrow(() -> new EntityNotFoundException("Rol no encontrado: " + rolId));
        reg.setRolNombre(rol.getNombreRol());
        registroRepo.save(reg);
    }
}
