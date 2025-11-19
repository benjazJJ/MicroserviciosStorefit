package com.storefit.catalog_service.Service;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.storefit.catalog_service.Model.Producto;
import com.storefit.catalog_service.Model.ProductoId;
import com.storefit.catalog_service.Repository.CategoriaRepository;
import com.storefit.catalog_service.Repository.ProductoRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductoService {

    private static final Set<String> TALLAS = Set.of("XS", "S", "M", "L", "XL");

    private final ProductoRepository repo;
    private final CategoriaRepository categoriaRepo;

    @Transactional(readOnly = true)
    public List<Producto> findAll() {
        return repo.findAll();
    }

    @Transactional(readOnly = true)
    public Producto findByIds(Long categoriaId, Long productoId) {
        var id = new ProductoId(categoriaId, productoId);
        return repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Producto no encontrado: " + categoriaId + "/" + productoId));
    }

    @Transactional(readOnly = true)
    public List<Producto> findByCategoria(Long categoriaId) {
        if (!categoriaRepo.existsById(categoriaId)) {
            throw new EntityNotFoundException("Categoría no encontrada: " + categoriaId);
        }
        return repo.findByIdIdCategoria(categoriaId);
    }

    @Transactional
    public Producto create(Producto p) {
        if (p == null || p.getId() == null
                || p.getId().getIdCategoria() == null
                || p.getId().getIdProducto() == null) {
            throw new IllegalArgumentException("Debe indicar id_categoria e id_producto en el id compuesto");
        }

        Long idCat = p.getId().getIdCategoria();
        if (!categoriaRepo.existsById(idCat)) {
            throw new EntityNotFoundException("Categoría no existe: " + idCat);
        }

        if (repo.existsById(p.getId())) {
            throw new IllegalArgumentException("El id de producto ya está en uso: "
                    + p.getId().getIdCategoria() + "/" + p.getId().getIdProducto());
        }

        normalizar(p);
        validarObligatorios(p);

        return repo.save(p);
    }

    @Transactional
    public Producto update(Long categoriaId, Long productoId, Producto in) {
        var db = findByIds(categoriaId, productoId);

        if (in == null) {
            throw new IllegalArgumentException("Cuerpo de producto requerido");
        }

        // No se permite cambiar el id compuesto vía payload
        db.setMarca(in.getMarca());
        db.setModelo(in.getModelo());
        db.setColor(in.getColor());
        db.setTalla(in.getTalla());
        db.setPrecio(in.getPrecio());
        db.setStock(in.getStock());
        db.setImageUrl(in.getImageUrl());

        normalizar(db);
        validarObligatorios(db);

        return repo.save(db);
    }

    @Transactional
    public void delete(Long categoriaId, Long productoId) {
        repo.delete(findByIds(categoriaId, productoId));
    }

    //Reserva y descuento de stock

    @Transactional
    public void verificarYDescontarStock(List<StockReservaItem> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("La lista de items de stock no puede estar vacía");
        }

        // Primero validamos todo (existencia y stock suficiente)
        for (StockReservaItem item : items) {
            if (item.getIdProducto() == null || item.getCantidad() == null || item.getCantidad() <= 0) {
                throw new IllegalArgumentException("Cada item debe indicar idProducto y cantidad > 0");
            }

            var producto = repo.findByIdIdProducto(item.getIdProducto())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Producto no encontrado con id_producto=" + item.getIdProducto()));

            if (producto.getStock() < item.getCantidad()) {
                throw new StockInsuficienteException(
                        "Stock insuficiente para el producto " + item.getIdProducto()
                                + " (disponible=" + producto.getStock()
                                + ", solicitado=" + item.getCantidad() + ")"
                );
            }
        }

        // Si todo está OK, recién aquí descontamos stock
        for (StockReservaItem item : items) {
            var producto = repo.findByIdIdProducto(item.getIdProducto())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Producto no encontrado con id_producto=" + item.getIdProducto()));
            producto.setStock(producto.getStock() - item.getCantidad());
            repo.save(producto);
        }
    }

    //Helpers

    private void normalizar(Producto p) {
        if (p.getMarca() != null) p.setMarca(p.getMarca().trim());
        if (p.getModelo() != null) p.setModelo(p.getModelo().trim());
        if (p.getColor() != null) p.setColor(p.getColor().trim());
        if (p.getTalla() != null) p.setTalla(p.getTalla().trim().toUpperCase());
        // Imagen: si viene en blanco, la dejamos en null
        if (p.getImageUrl() != null && p.getImageUrl().trim().isEmpty()) {
            p.setImageUrl(null);
        }
    }

    private void validarObligatorios(Producto p) {
        if (isBlank(p.getMarca()))  throw new IllegalArgumentException("La marca es obligatoria");
        if (isBlank(p.getModelo())) throw new IllegalArgumentException("El modelo es obligatorio");
        if (isBlank(p.getColor()))  throw new IllegalArgumentException("El color es obligatorio");
        if (isBlank(p.getTalla()))  throw new IllegalArgumentException("La talla es obligatoria");

        if (!TALLAS.contains(p.getTalla())) {
            throw new IllegalArgumentException("La talla debe ser XS, S, M, L o XL");
        }
        if (p.getPrecio() == null || p.getPrecio() < 0) {
            throw new IllegalArgumentException("El precio no puede ser negativo ni nulo");
        }
        if (p.getStock() == null || p.getStock() < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo ni nulo");
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
