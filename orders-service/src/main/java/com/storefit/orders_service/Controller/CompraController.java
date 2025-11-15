package com.storefit.orders_service.Controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.storefit.orders_service.Model.Compra;
import com.storefit.orders_service.Service.CompraService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/compras")
@RequiredArgsConstructor
public class CompraController {

    private final CompraService service;

    @GetMapping
    public List<Compra> all() {
        return service.listarTodas();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Compra> byId(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtenerPorId(id));
    }

    @GetMapping("/usuario/{rut}")
    public List<Compra> byRut(@PathVariable String rut) {
        return service.historialPorRut(rut);
    }

    @GetMapping("/usuario/{rut}/total")
    public Integer totalPorRut(@PathVariable String rut) {
        return service.totalGastado(rut);
    }

    @PostMapping
    public ResponseEntity<Compra> create(@RequestBody @Valid Compra compra) {
        Compra creada = service.crearCompra(compra);
        return ResponseEntity
                .created(URI.create("/api/v1/compras/" + creada.getIdCompra()))
                .body(creada);
    }
}
