package com.storefit.orders_service.Service;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import com.storefit.orders_service.Client.CatalogClient;
import com.storefit.orders_service.Model.Compra;
import com.storefit.orders_service.Model.CompraDetalle;
import com.storefit.orders_service.Repository.CompraRepository;

@ExtendWith(MockitoExtension.class)
class CompraServiceTest {

    @Mock
    private CompraRepository compraRepository;

    @Mock
    private CatalogClient catalogClient;

    @InjectMocks
    private CompraService service;

    @Test
    void crearCompra_cuandoTodoOk_llamaCatalogYGuardaCompra() {
        // Arreglo
        CompraDetalle d1 = CompraDetalle.builder()
                .idProducto(1001L)
                .nombreProducto("Producto A")
                .cantidad(2)
                .precioUnitario(10000)
                .build();

        CompraDetalle d2 = CompraDetalle.builder()
                .idProducto(2001L)
                .nombreProducto("Producto B")
                .cantidad(1)
                .precioUnitario(20000)
                .build();

        Compra compra = Compra.builder()
                .rutUsuario("12345678-9")
                .detalles(List.of(d1, d2))
                .build();

        // Mock: catalogClient.reservarStock no lanza excepción
        doNothing().when(catalogClient).reservarStock(anyList());

        // Mock: repo.save devuelve la misma compra pero con id
        when(compraRepository.save(any(Compra.class))).thenAnswer(invocation -> {
            Compra c = invocation.getArgument(0);
            c.setIdCompra(1L);
            return c;
        });

        // Act
        Compra result = service.crearCompra(compra);

        // Assert
        // Verificamos que llamó al catalog-client
        verify(catalogClient, times(1)).reservarStock(anyList());

        // Verificamos que guardó la compra
        verify(compraRepository, times(1)).save(any(Compra.class));

        assertThat(result.getIdCompra()).isEqualTo(1L);
        assertThat(result.getDetalles()).hasSize(2);

        // Todos los detalles deben tener la compra seteada
        assertThat(result.getDetalles())
                .allSatisfy(det -> assertThat(det.getCompra()).isSameAs(result));

        // fechaMillis debe estar seteado
        assertThat(result.getFechaMillis()).isNotNull();
    }

    @Test
    void crearCompra_sinDetalles_lanzaBadRequest() {
        Compra compra = Compra.builder()
                .rutUsuario("12345678-9")
                .detalles(List.of())
                .build();

        assertThatThrownBy(() -> service.crearCompra(compra))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("al menos un detalle");

        verifyNoInteractions(catalogClient);
        verifyNoInteractions(compraRepository);
    }

    @Test
    void crearCompra_conCantidadInvalida_lanzaBadRequest() {
        CompraDetalle d1 = CompraDetalle.builder()
                .idProducto(1001L)
                .nombreProducto("Producto A")
                .cantidad(0)
                .precioUnitario(10000)
                .build();

        Compra compra = Compra.builder()
                .rutUsuario("12345678-9")
                .detalles(List.of(d1))
                .build();

        assertThatThrownBy(() -> service.crearCompra(compra))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("cantidad > 0");

        verifyNoInteractions(catalogClient);
        verifyNoInteractions(compraRepository);
    }
}
