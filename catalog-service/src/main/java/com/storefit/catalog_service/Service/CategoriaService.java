package com.storefit.catalog_service.Service;

import com.storefit.catalog_service.Model.Categoria;
import com.storefit.catalog_service.Repository.CategoriaRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoriaService {

    private final CategoriaRepository repo;

    public List<Categoria> findAll() {
        return repo.findAll();
    }

    public Categoria findById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Categoría no encontrada: " + id));
    }

    @Transactional
    public Categoria create(Categoria c) {
        String nombre = c.getNombreCategoria();
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre de la categoría es obligatorio.");
        }
        String normalizado = nombre.trim();
        if (repo.existsByNombreCategoriaIgnoreCase(normalizado)) {
            throw new IllegalArgumentException("La categoría ya existe: " + normalizado);
        }
        c.setNombreCategoria(normalizado);
        return repo.save(c);
    }

    @Transactional
    public Categoria update(Long id, Categoria in) {
        var db = findById(id);

        String nuevoNombre = in.getNombreCategoria();
        if (nuevoNombre != null) {
            String normalizado = nuevoNombre.trim();
            if (!normalizado.equalsIgnoreCase(db.getNombreCategoria())
                    && repo.existsByNombreCategoriaIgnoreCase(normalizado)) {
                throw new IllegalArgumentException("Nombre de categoría ya utilizado: " + normalizado);
            }
            db.setNombreCategoria(normalizado);
        }

        return repo.save(db);
    }

    @Transactional
    public void delete(Long id) {
        repo.delete(findById(id));
    }
}