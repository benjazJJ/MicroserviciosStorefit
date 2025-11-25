package com.storefit.orders_service.Service;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
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
import com.storefit.orders_service.Client.UsersClient;
import com.storefit.orders_service.Model.Compra;
import com.storefit.orders_service.Model.CompraDetalle;
import com.storefit.orders_service.Model.UsuarioDTO;
import com.storefit.orders_service.Repository.CompraRepository;

@ExtendWith(MockitoExtension.class)
class CompraServiceTest {

        @Mock
        private CompraRepository compraRepository;

        @Mock
        private CatalogClient catalogClient;

        @Mock
        private UsersClient usersClient;

        @InjectMocks
        private CompraService service;

        private UsuarioDTO usuarioValido() {
                UsuarioDTO u = new UsuarioDTO();
                u.setRut("12.345.678-9");
                u.setNombre("Usuario Test");
                return u;
        }

        @Test
        void crearCompra_cuandoTodoOk_llamaCatalogYGuardaCompra() {
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
                                .rutUsuario("12.345.678-9")
                                .detalles(List.of(d1, d2))
                                .build();

                when(usersClient.obtenerUsuarioPorRut(eq("12.345.678-9"))).thenReturn(usuarioValido());
                doNothing().when(catalogClient).reservarStock(anyList());
                when(compraRepository.save(any(Compra.class))).thenAnswer(invocation -> {
                        Compra c = invocation.getArgument(0);
                        c.setIdCompra(1L);
                        return c;
                });

                Compra result = service.crearCompra(compra);

                verify(usersClient, times(1)).obtenerUsuarioPorRut("12.345.678-9");
                verify(catalogClient, times(1)).reservarStock(anyList());
                verify(compraRepository, times(1)).save(any(Compra.class));

                assertThat(result.getIdCompra()).isEqualTo(1L);
                assertThat(result.getDetalles()).hasSize(2);
                assertThat(result.getDetalles())
                                .allSatisfy(det -> assertThat(det.getCompra()).isSameAs(result));
                assertThat(result.getFechaMillis()).isNotNull();
        }

        @Test
        void crearCompra_sinDetalles_lanzaBadRequest() {
                Compra compra = Compra.builder()
                                .rutUsuario("12.345.678-9")
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
                .rutUsuario("12.345.678-9")
                .detalles(List.of(d1))
                .build();

        when(usersClient.obtenerUsuarioPorRut(eq("12.345.678-9"))).thenReturn(usuarioValido());

        assertThatThrownBy(() -> service.crearCompra(compra))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("cantidad > 0");

        verifyNoInteractions(catalogClient);
        verifyNoInteractions(compraRepository);
        }

        @Test
        void crearCompra_usuarioNoExiste_lanzaNotFound() {
                CompraDetalle d1 = CompraDetalle.builder()
                                .idProducto(1L)
                                .cantidad(1)
                                .precioUnitario(1000)
                                .build();
                Compra compra = Compra.builder()
                                .rutUsuario("12.345.678-9")
                                .detalles(List.of(d1))
                                .build();

                when(usersClient.obtenerUsuarioPorRut(eq("12.345.678-9")))
                                .thenThrow(new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND,
                                                "no existe"));

                assertThatThrownBy(() -> service.crearCompra(compra))
                                .isInstanceOf(ResponseStatusException.class)
                                .hasMessageContaining("no existe");

                verifyNoInteractions(catalogClient);
                verifyNoInteractions(compraRepository);
        }

        @Test
        void crearCompra_catalogoFalla_noPersisteCompra() {
                CompraDetalle d1 = CompraDetalle.builder()
                                .idProducto(1L)
                                .cantidad(1)
                                .precioUnitario(1000)
                                .build();
                Compra compra = Compra.builder()
                                .rutUsuario("12.345.678-9")
                                .detalles(List.of(d1))
                                .build();

                when(usersClient.obtenerUsuarioPorRut(eq("12.345.678-9"))).thenReturn(usuarioValido());
                org.springframework.web.server.ResponseStatusException stockEx = new org.springframework.web.server.ResponseStatusException(
                                org.springframework.http.HttpStatus.BAD_GATEWAY, "catalog down");
                org.mockito.Mockito.doThrow(stockEx).when(catalogClient).reservarStock(anyList());

                assertThatThrownBy(() -> service.crearCompra(compra))
                                .isInstanceOf(ResponseStatusException.class)
                                .hasMessageContaining("catalog down");

                verify(compraRepository, times(0)).save(any());
        }
}
