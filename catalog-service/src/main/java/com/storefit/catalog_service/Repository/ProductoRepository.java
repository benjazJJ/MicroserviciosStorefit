package com.storefit.catalog_service.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.storefit.catalog_service.Model.Producto;
import com.storefit.catalog_service.Model.ProductoId;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, ProductoId> {

    List<Producto> findByIdIdCategoria(Long idCategoria);

    Optional<Producto> findByIdIdProducto(Long idProducto);
}
