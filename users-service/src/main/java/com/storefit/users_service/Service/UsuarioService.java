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
import com.storefit.users_service.security.RutUtils;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository repo;
    private final RegistroRepository registroRepo;
    private final RolRepository rolRepo;

    public List<Usuario> findAll() { return repo.findAll(); }

    public Usuario findByRut(String rut) {
        // Attempt direct match first
        var opt = repo.findById(rut);
        if (opt.isPresent()) return opt.get();
        // Fallback: if dotted, also try without dots
        if (RutUtils.isDottedFormat(rut)) {
            String withoutDots = RutUtils.removeDots(rut);
            var opt2 = repo.findById(withoutDots);
            if (opt2.isPresent()) return opt2.get();
        }
        throw new EntityNotFoundException("Usuario no encontrado: " + rut);
    }

    public Usuario findByCorreo(String correo) {
        return repo.findByCorreoIgnoreCase(correo)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado por correo: " + correo));
    }

    public java.util.Optional<Usuario> findByCorreoOptional(String correo) {
        return repo.findByCorreoIgnoreCase(correo);
    }

    public boolean existsByRut(String rut) { return repo.existsById(rut); }

    public boolean existsByTelefono(String telefono) {
        return telefono != null && !telefono.isBlank() && repo.existsByTelefono(telefono);
    }

    public boolean isTelefonoDisponibleParaActualizar(String rut, String telefono) {
        if (telefono == null || telefono.isBlank()) return true;
        var other = repo.findByTelefono(telefono);
        return other.isEmpty() || other.get().getRut().equalsIgnoreCase(rut);
    }

    @Transactional
    public Usuario create(Usuario u) {
        // Enforce dotted format and persist canonical
        String canonicalRut = RutUtils.requireDottedOrBadRequest(u.getRut());
        u.setRut(canonicalRut);

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
        String canonicalRut = RutUtils.requireDottedOrBadRequest(rut);
        var reg = registroRepo.findByRut(canonicalRut)
                .orElseThrow(() -> new EntityNotFoundException("Registro no encontrado para rut: " + rut));
        reg.setRolId(rolId);
        // Actualiza el nombre del rol para consistencia
        var rol = rolRepo.findById(rolId).orElseThrow(() -> new EntityNotFoundException("Rol no encontrado: " + rolId));
        reg.setRolNombre(rol.getNombreRol());
        registroRepo.save(reg);
    }

    @Transactional
    public Usuario updateFoto(String rut, String fotoUri) {
        Usuario db = findByRut(rut);
        db.setFotoUri(fotoUri);
        return repo.save(db);
    }
}
