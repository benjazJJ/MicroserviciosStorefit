package com.storefit.support_service.Service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.storefit.support_service.Model.Mensaje;
import com.storefit.support_service.Model.MensajeConRespuestaDTO;
import com.storefit.support_service.Repository.MensajeRepository;
import com.storefit.support_service.Client.UsersClient;

@ExtendWith(MockitoExtension.class)
public class MensajeServiceTest {

    @Mock
    private MensajeRepository repo;

    @InjectMocks
    private MensajeService service;
    @Mock
    private UsersClient usersClient;

    // ---------- helpers ----------

    private Mensaje crearMensajeBase(
            Long id,
            String rutRemitente,
            Integer idRolDestino,
            String rutDestino,
            String contenido,
            boolean esRespuesta) {
        Mensaje m = Mensaje.builder()
                .id(id)
                .rutRemitente(rutRemitente)
                .idRolDestino(idRolDestino)
                .rutDestino(rutDestino)
                .contenido(contenido)
                .creadoEn(1763074750001L)
                .leido(false)
                .esRespuesta(esRespuesta)
                .respondeAId(esRespuesta ? 1L : null)
                .idHilo(1L)
                .respondidoEn(null)
                .build();
        return m;
    }

    // ---------- listarTodos ----------

    @Test
    void listarTodos_deberiaRetornarListaMensajes() {
        Mensaje m1 = crearMensajeBase(1L, "11111111-1", 3, null, "Hola soporte", false);
        Mensaje m2 = crearMensajeBase(2L, "22222222-2", 3, null, "Otro mensaje", false);

        when(repo.findAll()).thenReturn(List.of(m1, m2));

        List<Mensaje> resultado = service.listarTodos();

        assertEquals(2, resultado.size());
        assertEquals(1L, resultado.get(0).getId());
        assertEquals("Hola soporte", resultado.get(0).getContenido());
        verify(repo, times(1)).findAll();
    }

    // ---------- obtenerPorId ----------

    @Test
    void obtenerPorId_cuandoExiste_deberiaRetornarMensaje() {
        Mensaje m1 = crearMensajeBase(1L, "11111111-1", 3, null, "Hola soporte", false);
        when(repo.findById(1L)).thenReturn(Optional.of(m1));

        Mensaje resultado = service.obtenerPorId(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        verify(repo, times(1)).findById(1L);
    }

    @Test
    void obtenerPorId_cuandoNoExiste_deberiaLanzarNotFound() {
        when(repo.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> service.obtenerPorId(99L));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Mensaje no encontrado"));
        verify(repo, times(1)).findById(99L);
    }

    // ---------- enviarMensajeCliente ----------

    @Test
    void enviarMensajeCliente_deberiaCrearMensajeConDatosCorrectos() {
        // mock del save: la primera vez setea id, la segunda respeta threadId
        when(repo.save(any(Mensaje.class))).thenAnswer(invocation -> {
            Mensaje m = invocation.getArgument(0);
            if (m.getId() == null) {
                m.setId(1L);
            }
            return m;
        });

        // no lanzar al validar RUT
        doNothing().when(usersClient).validarUsuarioExistePorRut("12345678-9");

        Mensaje resultado = service.enviarMensajeCliente("12345678-9", "  Hola, tengo un problema  ");

        assertNotNull(resultado.getId());
        assertEquals(1L, resultado.getId());
        assertEquals("12345678-9", resultado.getRutRemitente());
        assertEquals(MensajeService.ROL_SOPORTE, resultado.getIdRolDestino());
        assertEquals("Hola, tengo un problema", resultado.getContenido());
        assertFalse(resultado.getLeido());
        assertFalse(Boolean.TRUE.equals(resultado.getEsRespuesta()));
        assertEquals(resultado.getId(), resultado.getIdHilo());

        verify(repo, times(2)).save(any(Mensaje.class));
    }

    // ---------- responderMensaje ----------

    @Test
    void responderMensaje_deberiaCrearRespuestaYActualizarOriginal() {
        Mensaje original = Mensaje.builder()
                .id(1L)
                .rutRemitente("12345678-9") // cliente
                .idRolDestino(MensajeService.ROL_SOPORTE)
                .rutDestino(null)
                .contenido("Hola soporte")
                .creadoEn(1763074750001L)
                .leido(false)
                .esRespuesta(false)
                .respondeAId(null)
                .idHilo(1L)
                .respondidoEn(null)
                .build();

        when(repo.findById(1L)).thenReturn(Optional.of(original));
        when(repo.save(any(Mensaje.class))).thenAnswer(invocation -> invocation.getArgument(0));

        doNothing().when(usersClient).validarUsuarioExistePorRut("11111111-1");
        Mensaje respuesta = service.responderMensaje(1L, "11111111-1", "Respuesta soporte");

        assertNotNull(respuesta);
        assertEquals("11111111-1", respuesta.getRutRemitente());
        assertEquals("12345678-9", respuesta.getRutDestino());
        assertTrue(Boolean.TRUE.equals(respuesta.getEsRespuesta()));
        assertEquals(1L, respuesta.getIdHilo());
        assertEquals("Respuesta soporte", respuesta.getContenido());
        assertNotNull(original.getRespondidoEn());

        // Se guarda el original actualizado y la respuesta
        verify(repo, times(2)).save(any(Mensaje.class));
        verify(repo, times(1)).findById(1L);
    }

    // ---------- marcarComoLeido ----------

    @Test
    void marcarComoLeido_cuandoEstabaNoLeido_deberiaMarcarTrue() {
        Mensaje noLeido = crearMensajeBase(1L, "11111111-1", 3, null, "Hola soporte", false);
        noLeido.setLeido(false);

        when(repo.findById(1L)).thenReturn(Optional.of(noLeido));
        when(repo.save(any(Mensaje.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Mensaje resultado = service.marcarComoLeido(1L);

        assertTrue(Boolean.TRUE.equals(resultado.getLeido()));
        verify(repo, times(1)).findById(1L);
        verify(repo, times(1)).save(any(Mensaje.class));
    }

    // ---------- eliminar ----------

    @Test
    void eliminar_cuandoExiste_deberiaEliminarSinErrores() {
        when(repo.existsById(1L)).thenReturn(true);
        doNothing().when(repo).deleteById(1L);

        assertDoesNotThrow(() -> service.eliminar(1L));

        verify(repo, times(1)).existsById(1L);
        verify(repo, times(1)).deleteById(1L);
    }

    @Test
    void eliminar_cuandoNoExiste_deberiaLanzarNotFound() {
        when(repo.existsById(99L)).thenReturn(false);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> service.eliminar(99L));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        verify(repo, times(1)).existsById(99L);
        verify(repo, never()).deleteById(anyLong());
    }

    // ---------- bandejaSoporte ----------

    @Test
    void bandejaSoporte_deberiaMapearOriginalYRespuesta() {
        Mensaje original = crearMensajeBase(1L, "12345678-9", MensajeService.ROL_SOPORTE, null, "Hola soporte", false);
        Mensaje respuesta = crearMensajeBase(2L, "11111111-1", null, "12345678-9", "Respuesta soporte", true);

        when(repo.findByIdRolDestinoAndEsRespuestaFalseOrderByCreadoEnAsc(MensajeService.ROL_SOPORTE))
                .thenReturn(List.of(original));
        when(repo.findByRespondeAIdAndEsRespuestaTrueOrderByCreadoEnAsc(1L))
                .thenReturn(List.of(respuesta));

        List<MensajeConRespuestaDTO> resultado = service.bandejaSoporte(true);

        assertEquals(1, resultado.size());
        MensajeConRespuestaDTO dto = resultado.get(0);
        assertEquals(1L, dto.getClienteMensaje().getId());
        assertEquals(2L, dto.getRespuesta().getId());
        assertEquals("Hola soporte", dto.getClienteMensaje().getContenido());
        assertEquals("Respuesta soporte", dto.getRespuesta().getContenido());

        verify(repo, times(1))
                .findByIdRolDestinoAndEsRespuestaFalseOrderByCreadoEnAsc(MensajeService.ROL_SOPORTE);
        verify(repo, times(1))
                .findByRespondeAIdAndEsRespuestaTrueOrderByCreadoEnAsc(1L);
    }

    // ---------- bandejaUsuario ----------

    @Test
    void bandejaUsuario_deberiaMapearOriginalYRespuesta() {
        String rut = "12345678-9";

        Mensaje original = crearMensajeBase(1L, rut, MensajeService.ROL_SOPORTE, null, "Hola soporte", false);
        Mensaje respuesta = crearMensajeBase(2L, "11111111-1", null, rut, "Respuesta soporte", true);

        when(repo.findByRutRemitenteAndEsRespuestaFalseOrderByCreadoEnDesc(rut))
                .thenReturn(List.of(original));
        when(repo.findByRespondeAIdAndEsRespuestaTrueOrderByCreadoEnAsc(1L))
                .thenReturn(List.of(respuesta));

        List<MensajeConRespuestaDTO> resultado = service.bandejaUsuario(rut, false);

        assertEquals(1, resultado.size());
        MensajeConRespuestaDTO dto = resultado.get(0);
        assertEquals(rut, dto.getClienteMensaje().getRutRemitente());
        assertEquals("Hola soporte", dto.getClienteMensaje().getContenido());
        assertEquals("Respuesta soporte", dto.getRespuesta().getContenido());

        verify(repo, times(1))
                .findByRutRemitenteAndEsRespuestaFalseOrderByCreadoEnDesc(rut);
        verify(repo, times(1))
                .findByRespondeAIdAndEsRespuestaTrueOrderByCreadoEnAsc(1L);
    }

    // ---------- mensajesPorThread ----------

    @Test
    void mensajesPorThread_deberiaRetornarListaMensajesDeHilo() {
        Mensaje original = crearMensajeBase(1L, "12345678-9", MensajeService.ROL_SOPORTE, null, "Hola soporte", false);
        Mensaje respuesta = crearMensajeBase(2L, "11111111-1", null, "12345678-9", "Respuesta soporte", true);

        when(repo.findByIdHiloOrderByCreadoEnAsc(1L))
                .thenReturn(List.of(original, respuesta));

        List<Mensaje> resultado = service.mensajesPorThread(1L);

        assertEquals(2, resultado.size());
        assertEquals(1L, resultado.get(0).getId());
        assertEquals(2L, resultado.get(1).getId());
        verify(repo, times(1)).findByIdHiloOrderByCreadoEnAsc(1L);
    }
}
