package com.storefit.catalog_service.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.storefit.catalog_service.Service.StockReservaItem;
import com.storefit.catalog_service.Model.Producto;
import com.storefit.catalog_service.Model.ProductoId;
import com.storefit.catalog_service.Service.ProductoService;
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
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = ProductoController.class)
class ProductoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductoService productoService;

    @Autowired
    private ObjectMapper objectMapper;

    private Producto sampleProducto() {
        return Producto.builder()
                .id(new ProductoId(1L, 1001L))
                .marca("Adidas")
                .modelo("Ultraboost 5")
                .color("Negro")
                .talla("M")
                .precio(59990)
                .stock(10)
                .imageUrl("/img/test.png")
                .build();
    }

    @Test
    void all_debeRetornarListaDeProductos() throws Exception {
        var p1 = sampleProducto();
        var p2 = Producto.builder()
                .id(new ProductoId(2L, 2001L))
                .marca("Nike")
                .modelo("Zoom X")
                .color("Blanco")
                .talla("L")
                .precio(49990)
                .stock(5)
                .imageUrl(null)
                .build();

        when(productoService.findAll()).thenReturn(List.of(p1, p2));

        mockMvc.perform(get("/api/v1/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id.idCategoria").value(1))
                .andExpect(jsonPath("$[0].id.idProducto").value(1001))
                .andExpect(jsonPath("$[0].marca").value("Adidas"));
    }

    @Test
    void byId_debeRetornarProductoPorIdCompuesto() throws Exception {
        var p = sampleProducto();
        when(productoService.findByIds(1L, 1001L)).thenReturn(p);

        mockMvc.perform(get("/api/v1/productos/{categoriaId}/{productoId}", 1L, 1001L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id.idCategoria").value(1))
                .andExpect(jsonPath("$.id.idProducto").value(1001))
                .andExpect(jsonPath("$.marca").value("Adidas"));
    }

    @Test
    void byCategoria_debeRetornarProductosDeCategoria() throws Exception {
        var p1 = sampleProducto();
        when(productoService.findByCategoria(1L)).thenReturn(List.of(p1));

        mockMvc.perform(get("/api/v1/productos/categoria/{categoriaId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id.idCategoria").value(1));
    }

    @Test
    void byCategoria_debeRetornar404SiCategoriaNoExiste() throws Exception {
        when(productoService.findByCategoria(999L))
                .thenThrow(new jakarta.persistence.EntityNotFoundException("Categoría no encontrada: 999"));

        mockMvc.perform(get("/api/v1/productos/categoria/{categoriaId}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Categoría no encontrada: 999"));
    }

    @Test
    void create_debeCrearProductoYRetornar201ConMensajeYData() throws Exception {
        var input = sampleProducto();
        var creado = sampleProducto(); // simulamos que es lo mismo que entró
        when(productoService.create(any(Producto.class))).thenReturn(creado);

        String json = objectMapper.writeValueAsString(input);

        mockMvc.perform(
                        post("/api/v1/productos")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/productos/1/1001"))
                .andExpect(jsonPath("$.message").value("Producto añadido correctamente"))
                .andExpect(jsonPath("$.data.marca").value("Adidas"))
                .andExpect(jsonPath("$.data.id.idCategoria").value(1))
                .andExpect(jsonPath("$.data.id.idProducto").value(1001));
    }

    @Test
    void update_debeActualizarProductoYRetornarMensajeYData() throws Exception {
        var input = sampleProducto();
        var actualizado = sampleProducto();
        actualizado.setPrecio(39990);

        when(productoService.update(eq(1L), eq(1001L), any(Producto.class))).thenReturn(actualizado);

        String json = objectMapper.writeValueAsString(input);

        mockMvc.perform(
                        put("/api/v1/productos/{categoriaId}/{productoId}", 1L, 1001L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Producto actualizado correctamente"))
                .andExpect(jsonPath("$.data.precio").value(39990));
    }

    @Test
    void delete_debeEliminarProductoYRetornarMensajeOk() throws Exception {
        willDoNothing().given(productoService).delete(1L, 1001L);

        mockMvc.perform(delete("/api/v1/productos/{categoriaId}/{productoId}", 1L, 1001L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Producto eliminado correctamente"));
    }

    @Test
    void reservarStock_debeLlamarServicioYRetornarMensajeOK() throws Exception {
        List<StockReservaItem> items = List.of(
                new StockReservaItem(1001L, 2),
                new StockReservaItem(2001L, 1)
        );

        // el service no devuelve nada (void)
        willDoNothing().given(productoService)
                .verificarYDescontarStock(ArgumentMatchers.anyList());

        String json = objectMapper.writeValueAsString(items);

        mockMvc.perform(
                        post("/api/v1/productos/stock/reservar")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Stock reservado correctamente"));
    }
}
