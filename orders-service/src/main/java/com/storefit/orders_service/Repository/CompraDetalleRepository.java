package com.storefit.orders_service.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.storefit.orders_service.Model.CompraDetalle;

public interface CompraDetalleRepository extends JpaRepository<CompraDetalle, Long> {

    List<CompraDetalle> findByCompraIdCompra(Long idCompra);
}
