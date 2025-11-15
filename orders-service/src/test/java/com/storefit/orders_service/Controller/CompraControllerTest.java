package com.storefit.orders_service.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.storefit.orders_service.Model.Compra;
import com.storefit.orders_service.Model.CompraDetalle;
import com.storefit.orders_service.Service.CompraService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.willReturn;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = CompraController.class)
class CompraControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CompraService compraService;

    @Autowired
    private ObjectMapper objectMapper;

    private CompraDetalle sampleDetalle1() {
        return CompraDetalle.builder()
                .idDetalle(10L)
                .idProducto(1001L)
                .nombreProducto("Producto A")
                .cantidad(2)
                .precioUnitario(10000)
                .build();
    }

    private CompraDetalle sampleDetalle2() {
        return CompraDetalle.builder()
                .idDetalle(11L)
                .idProducto(2001L)
                .nombreProducto("Producto B")
                .cantidad(1)
                .precioUnitario(20000)
                .build();
    }

    private Compra sampleCompra() {
        return Compra.builder()
                .idCompra(1L)
                .rutUsuario("12345678-9")
                .fechaMillis(1731700000000L)
                .detalles(List.of(sampleDetalle1(), sampleDetalle2()))
                .build();
    }

    @Test
    void all_debeRetornarListaDeCompras() throws Exception {
        var c1 = sampleCompra();
        var c2 = Compra.builder()
                .idCompra(2L)
                .rutUsuario("99999999-9")
                .fechaMillis(1731700500000L)
                .detalles(List.of())
                .build();

        when(compraService.listarTodas()).thenReturn(List.of(c1, c2));

        mockMvc.perform(get("/api/v1/compras"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].idCompra").value(1))
                .andExpect(jsonPath("$[0].rutUsuario").value("12345678-9"));
    }

    @Test
    void byId_debeRetornarCompraPorId() throws Exception {
        var compra = sampleCompra();
        when(compraService.obtenerPorId(1L)).thenReturn(compra);

        mockMvc.perform(get("/api/v1/compras/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idCompra").value(1))
                .andExpect(jsonPath("$.rutUsuario").value("12345678-9"))
                .andExpect(jsonPath("$.detalles.length()").value(2));
    }

    @Test
    void byRut_debeRetornarHistorialPorRut() throws Exception {
        var compra = sampleCompra();
        when(compraService.historialPorRut("12345678-9")).thenReturn(List.of(compra));

        mockMvc.perform(get("/api/v1/compras/usuario/{rut}", "12345678-9"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].rutUsuario").value("12345678-9"));
    }

    @Test
    void totalPorRut_debeRetornarEnteroConTotalGastado() throws Exception {
        when(compraService.totalGastado("12345678-9")).thenReturn(39980);

        mockMvc.perform(get("/api/v1/compras/usuario/{rut}/total", "12345678-9"))
                .andExpect(status().isOk())
                .andExpect(content().string("39980"));
    }

    @Test
    void create_debeCrearCompraYRetornar201ConBody() throws Exception {
        // Build request (sin idCompra ni fechaMillis, como lo manda la app)
        CompraDetalle requestDetalle1 = CompraDetalle.builder()
                .idProducto(1001L)
                .nombreProducto("Producto A")
                .cantidad(2)
                .precioUnitario(10000)
                .build();

        CompraDetalle requestDetalle2 = CompraDetalle.builder()
                .idProducto(2001L)
                .nombreProducto("Producto B")
                .cantidad(1)
                .precioUnitario(20000)
                .build();

        Compra requestCompra = Compra.builder()
                .rutUsuario("12345678-9")
                .detalles(List.of(requestDetalle1, requestDetalle2))
                .build();

        // Lo que devuelve el service (compra ya persistida)
        Compra persisted = sampleCompra();
        when(compraService.crearCompra(any(Compra.class))).thenReturn(persisted);

        String json = objectMapper.writeValueAsString(requestCompra);

        mockMvc.perform(
                        post("/api/v1/compras")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/compras/1"))
                .andExpect(jsonPath("$.idCompra").value(1))
                .andExpect(jsonPath("$.rutUsuario").value("12345678-9"))
                .andExpect(jsonPath("$.detalles.length()").value(2));
    }
}
