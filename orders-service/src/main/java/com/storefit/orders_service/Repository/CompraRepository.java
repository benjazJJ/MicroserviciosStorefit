package com.storefit.orders_service.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.storefit.orders_service.Model.Compra;

public interface CompraRepository extends JpaRepository<Compra, Long> {

    // Historial de un usuario, ordenado como en el DAO
    List<Compra> findByRutUsuarioOrderByFechaMillisDesc(String rutUsuario);

    // Total gastado por RUT (como getTotalGastadoPorRut en el DAO)
    @Query("""
        SELECT COALESCE(SUM(d.cantidad * d.precioUnitario), 0)
        FROM CompraDetalle d
        WHERE d.compra.rutUsuario = :rutUsuario
    """)
    Integer totalGastadoPorRut(String rutUsuario);
}
