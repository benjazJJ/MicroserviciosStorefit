package com.storefit.catalog_service.Service;


import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.storefit.catalog_service.Model.Producto;
import com.storefit.catalog_service.Model.ProductoId;
import com.storefit.catalog_service.Repository.CategoriaRepository;
import com.storefit.catalog_service.Repository.ProductoRepository;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private ProductoService service;

    @Test
    void verificarYDescontarStock_cuandoTodoOk_descuentaStockYGuarda() {
        // Arreglo
        StockReservaItem item1 = new StockReservaItem(1001L, 2);
        StockReservaItem item2 = new StockReservaItem(2001L, 1);

        Producto p1 = Producto.builder()
                .id(new ProductoId(1L, 1001L))
                .marca("Marca A")
                .modelo("Modelo A")
                .color("Azul")
                .talla("M")
                .precio(10000)
                .stock(5)
                .build();

        Producto p2 = Producto.builder()
                .id(new ProductoId(2L, 2001L))
                .marca("Marca B")
                .modelo("Modelo B")
                .color("Rojo")
                .talla("L")
                .precio(20000)
                .stock(3)
                .build();

        when(productoRepository.findByIdIdProducto(1001L)).thenReturn(Optional.of(p1));
        when(productoRepository.findByIdIdProducto(2001L)).thenReturn(Optional.of(p2));

        // Act
        service.verificarYDescontarStock(List.of(item1, item2));

        // Assert
        assertThat(p1.getStock()).isEqualTo(3); // 5 - 2
        assertThat(p2.getStock()).isEqualTo(2); // 3 - 1

        verify(productoRepository, times(1)).save(p1);
        verify(productoRepository, times(1)).save(p2);
    }

    @Test
    void verificarYDescontarStock_cuandoProductoNoExiste_lanzaEntityNotFound() {
        // Arrange
        StockReservaItem item = new StockReservaItem(9999L, 1);
        when(productoRepository.findByIdIdProducto(9999L)).thenReturn(Optional.empty());

        // Act / Assert
        assertThatThrownBy(() ->
                service.verificarYDescontarStock(List.of(item))
        )
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Producto no encontrado");
    }

    @Test
    void verificarYDescontarStock_cuandoNoHayStockSuficiente_lanzaStockInsuficiente() {
        // Arreglo
        StockReservaItem item = new StockReservaItem(1001L, 10);

        Producto p = Producto.builder()
                .id(new ProductoId(1L, 1001L))
                .marca("Marca A")
                .modelo("Modelo A")
                .color("Azul")
                .talla("M")
                .precio(10000)
                .stock(3)
                .build();

        when(productoRepository.findByIdIdProducto(1001L)).thenReturn(Optional.of(p));

        // Act / Assert
        assertThatThrownBy(() ->
                service.verificarYDescontarStock(List.of(item))
        )
                .isInstanceOf(StockInsuficienteException.class)
                .hasMessageContaining("Stock insuficiente");
    }

    @Test
    void verificarYDescontarStock_cuandoListaVacia_lanzaIllegalArgument() {
        assertThatThrownBy(() ->
                service.verificarYDescontarStock(List.of())
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no puede estar vacía");
    }

    @Test
    void verificarYDescontarStock_cuandoCantidadEsInvalida_lanzaIllegalArgument() {
        StockReservaItem item = new StockReservaItem(1001L, 0);

        assertThatThrownBy(() ->
                service.verificarYDescontarStock(List.of(item))
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cantidad > 0");
    }

    @Test
    void create_cuandoIdDuplicado_lanzaIllegalArgumentConMensajeClaro() {
        Producto p = Producto.builder()
                .id(new ProductoId(1L, 1001L))
                .marca("Marca")
                .modelo("Modelo")
                .color("Negro")
                .talla("M")
                .precio(1000)
                .stock(1)
                .build();

        when(categoriaRepository.existsById(1L)).thenReturn(true);
        when(productoRepository.existsById(p.getId())).thenReturn(true);

        assertThatThrownBy(() -> service.create(p))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("El id de producto ya");
    }

    @Test
    void findByIds_cuandoNoExiste_lanzaEntityNotFound() {
        ProductoId id = new ProductoId(9L, 9999L);
        when(productoRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findByIds(9L, 9999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("no encontrado");
    }

    @Test
    void findByCategoria_cuandoCategoriaNoExiste_lanzaEntityNotFound() {
        when(categoriaRepository.existsById(8L)).thenReturn(false);

        assertThatThrownBy(() -> service.findByCategoria(8L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("no encontrada");
    }
}
