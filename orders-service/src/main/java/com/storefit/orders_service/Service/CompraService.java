package com.storefit.orders_service.Service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.storefit.orders_service.Client.CatalogClient;
import com.storefit.orders_service.Client.StockReservaItemDto;
import com.storefit.orders_service.Model.Compra;
import com.storefit.orders_service.Model.CompraDetalle;
import com.storefit.orders_service.Repository.CompraRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CompraService {

    private final CompraRepository compraRepository;
    private final CatalogClient catalogClient; 

    public List<Compra> listarTodas() {
        return compraRepository.findAll();
    }

    public List<Compra> historialPorRut(String rut) {
        return compraRepository.findByRutUsuarioOrderByFechaMillisDesc(rut);
    }

    public Compra obtenerPorId(Long id) {
        return compraRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Compra no encontrada"
                ));
    }

    public Compra crearCompra(Compra compra) {
        if (compra.getDetalles() == null || compra.getDetalles().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La compra debe contener al menos un detalle"
            );
        }

        // 1) Preparamos el payload para el catalog-service
        List<StockReservaItemDto> stockItems = new ArrayList<>();
        for (CompraDetalle d : compra.getDetalles()) {
            if (d.getIdProducto() == null || d.getCantidad() == null || d.getCantidad() <= 0) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Cada detalle debe indicar idProducto y cantidad > 0"
                );
            }
            stockItems.add(new StockReservaItemDto(d.getIdProducto(), d.getCantidad()));
        }

        // 2) Llamamos al catalog-service para verificar y descontar stock
        //    Si no hay stock suficiente, lanzará ResponseStatusException.
        catalogClient.reservarStock(stockItems);

        // 3) Si el stock fue reservado correctamente, guardamos la compra
        compra.setIdCompra(null);
        compra.setFechaMillis(System.currentTimeMillis());

        for (CompraDetalle d : compra.getDetalles()) {
            d.setIdDetalle(null);
            d.setCompra(compra); // set FK
        }

        return compraRepository.save(compra);
    }

    public Integer totalGastado(String rut) {
        return compraRepository.totalGastadoPorRut(rut);
    }
}
