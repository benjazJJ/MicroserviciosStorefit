package com.storefit.orders_service.Service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.storefit.orders_service.Model.Compra;
import com.storefit.orders_service.Model.CompraDetalle;
import com.storefit.orders_service.Repository.CompraRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CompraService {

    private final CompraRepository compraRepository;

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
        compra.setIdCompra(null);
        compra.setFechaMillis(System.currentTimeMillis());

        if (compra.getDetalles() != null) {
            for (CompraDetalle d : compra.getDetalles()) {
                d.setIdDetalle(null);
                d.setCompra(compra); // set FK
            }
        }

        return compraRepository.save(compra);
    }

    public Integer totalGastado(String rut) {
        return compraRepository.totalGastadoPorRut(rut);
    }
}
