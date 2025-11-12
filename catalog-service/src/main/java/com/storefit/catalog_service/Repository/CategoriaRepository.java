package com.storefit.catalog_service.Repository;

import com.storefit.catalog_service.Model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    boolean existsByNombreCategoriaIgnoreCase(String nombreCategoria);
    Optional<Categoria> findByNombreCategoriaIgnoreCase(String nombreCategoria);
}
