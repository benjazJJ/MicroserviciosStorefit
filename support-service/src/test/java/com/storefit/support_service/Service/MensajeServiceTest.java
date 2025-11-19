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

@ExtendWith(MockitoExtension.class)
public class MensajeServiceTest {

    @Mock
    private MensajeRepository repo;

    @InjectMocks
    private MensajeService service;

    // ---------- helpers ----------

    private Mensaje crearMensajeBase(
            Long id,
            Long senderUserId,
            Integer targetRoleId,
            Long targetUserId,
            String content,
            boolean isResponse) {
        Mensaje m = Mensaje.builder()
                .id(id)
                .senderUserId(senderUserId)
                .targetRoleId(targetRoleId)
                .targetUserId(targetUserId)
                .content(content)
                .createdAt(1763074750001L)
                .read(false)
                .isResponse(isResponse)
                .repliedToId(isResponse ? 1L : null)
                .threadId(1L)
                .respondedAt(null)
                .build();
        return m;
    }

    // ---------- listarTodos ----------

    @Test
    void listarTodos_deberiaRetornarListaMensajes() {
        Mensaje m1 = crearMensajeBase(1L, 1L, 3, null, "Hola soporte", false);
        Mensaje m2 = crearMensajeBase(2L, 2L, 3, null, "Otro mensaje", false);

        when(repo.findAll()).thenReturn(List.of(m1, m2));

        List<Mensaje> resultado = service.listarTodos();

        assertEquals(2, resultado.size());
        assertEquals(1L, resultado.get(0).getId());
        assertEquals("Hola soporte", resultado.get(0).getContent());
        verify(repo, times(1)).findAll();
    }

    // ---------- obtenerPorId ----------

    @Test
    void obtenerPorId_cuandoExiste_deberiaRetornarMensaje() {
        Mensaje m1 = crearMensajeBase(1L, 1L, 3, null, "Hola soporte", false);
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

        Mensaje resultado = service.enviarMensajeCliente(10L, "  Hola, tengo un problema  ");

        assertNotNull(resultado.getId());
        assertEquals(1L, resultado.getId());
        assertEquals(10L, resultado.getSenderUserId());
        assertEquals(MensajeService.ROL_SOPORTE, resultado.getTargetRoleId());
        assertEquals("Hola, tengo un problema", resultado.getContent());
        assertFalse(resultado.getRead());
        assertFalse(Boolean.TRUE.equals(resultado.getIsResponse())); // wrapper Boolean
        assertEquals(resultado.getId(), resultado.getThreadId());

        verify(repo, times(2)).save(any(Mensaje.class));
    }

    // ---------- responderMensaje ----------

    @Test
    void responderMensaje_deberiaCrearRespuestaYActualizarOriginal() {
        Mensaje original = Mensaje.builder()
                .id(1L)
                .senderUserId(100L) // cliente
                .targetRoleId(MensajeService.ROL_SOPORTE)
                .targetUserId(null)
                .content("Hola soporte")
                .createdAt(1763074750001L)
                .read(false)
                .isResponse(false)
                .repliedToId(null)
                .threadId(1L)
                .respondedAt(null)
                .build();

        when(repo.findById(1L)).thenReturn(Optional.of(original));
        when(repo.save(any(Mensaje.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Mensaje respuesta = service.responderMensaje(1L, 9001L, "Respuesta soporte");

        assertNotNull(respuesta);
        assertEquals(9001L, respuesta.getSenderUserId());
        assertEquals(100L, respuesta.getTargetUserId());
        assertTrue(Boolean.TRUE.equals(respuesta.getIsResponse()));
        assertEquals(1L, respuesta.getThreadId());
        assertEquals("Respuesta soporte", respuesta.getContent());
        assertNotNull(original.getRespondedAt());

        // Se guarda el original actualizado y la respuesta
        verify(repo, times(2)).save(any(Mensaje.class));
        verify(repo, times(1)).findById(1L);
    }

    // ---------- marcarComoLeido ----------

    @Test
    void marcarComoLeido_cuandoEstabaNoLeido_deberiaMarcarTrue() {
        Mensaje noLeido = crearMensajeBase(1L, 1L, 3, null, "Hola soporte", false);
        noLeido.setRead(false);

        when(repo.findById(1L)).thenReturn(Optional.of(noLeido));
        when(repo.save(any(Mensaje.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Mensaje resultado = service.marcarComoLeido(1L);

        assertTrue(Boolean.TRUE.equals(resultado.getRead()));
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
        Mensaje original = crearMensajeBase(1L, 10L, MensajeService.ROL_SOPORTE, null, "Hola soporte", false);
        Mensaje respuesta = crearMensajeBase(2L, 9001L, null, 10L, "Respuesta soporte", true);

        when(repo.findByTargetRoleIdAndIsResponseFalseOrderByCreatedAtAsc(MensajeService.ROL_SOPORTE))
                .thenReturn(List.of(original));
        when(repo.findByRepliedToIdAndIsResponseTrueOrderByCreatedAtAsc(1L))
                .thenReturn(List.of(respuesta));

        List<MensajeConRespuestaDTO> resultado = service.bandejaSoporte(true);

        assertEquals(1, resultado.size());
        MensajeConRespuestaDTO dto = resultado.get(0);
        assertEquals(1L, dto.getClienteMensaje().getId());
        assertEquals(2L, dto.getRespuesta().getId());
        assertEquals("Hola soporte", dto.getClienteMensaje().getContent());
        assertEquals("Respuesta soporte", dto.getRespuesta().getContent());

        verify(repo, times(1))
                .findByTargetRoleIdAndIsResponseFalseOrderByCreatedAtAsc(MensajeService.ROL_SOPORTE);
        verify(repo, times(1))
                .findByRepliedToIdAndIsResponseTrueOrderByCreatedAtAsc(1L);
    }

    // ---------- bandejaUsuario ----------

    @Test
    void bandejaUsuario_deberiaMapearOriginalYRespuesta() {
        Long usuarioId = 10L;

        Mensaje original = crearMensajeBase(1L, usuarioId, MensajeService.ROL_SOPORTE, null, "Hola soporte", false);
        Mensaje respuesta = crearMensajeBase(2L, 9001L, null, usuarioId, "Respuesta soporte", true);

        when(repo.findBySenderUserIdAndIsResponseFalseOrderByCreatedAtDesc(usuarioId))
                .thenReturn(List.of(original));
        when(repo.findByRepliedToIdAndIsResponseTrueOrderByCreatedAtAsc(1L))
                .thenReturn(List.of(respuesta));

        List<MensajeConRespuestaDTO> resultado = service.bandejaUsuario(usuarioId, false);

        assertEquals(1, resultado.size());
        MensajeConRespuestaDTO dto = resultado.get(0);
        assertEquals(usuarioId, dto.getClienteMensaje().getSenderUserId());
        assertEquals("Hola soporte", dto.getClienteMensaje().getContent());
        assertEquals("Respuesta soporte", dto.getRespuesta().getContent());

        verify(repo, times(1))
                .findBySenderUserIdAndIsResponseFalseOrderByCreatedAtDesc(usuarioId);
        verify(repo, times(1))
                .findByRepliedToIdAndIsResponseTrueOrderByCreatedAtAsc(1L);
    }

    // ---------- mensajesPorThread ----------

    @Test
    void mensajesPorThread_deberiaRetornarListaMensajesDeHilo() {
        Mensaje original = crearMensajeBase(1L, 10L, MensajeService.ROL_SOPORTE, null, "Hola soporte", false);
        Mensaje respuesta = crearMensajeBase(2L, 9001L, null, 10L, "Respuesta soporte", true);

        when(repo.findByThreadIdOrderByCreatedAtAsc(1L))
                .thenReturn(List.of(original, respuesta));

        List<Mensaje> resultado = service.mensajesPorThread(1L);

        assertEquals(2, resultado.size());
        assertEquals(1L, resultado.get(0).getId());
        assertEquals(2L, resultado.get(1).getId());
        verify(repo, times(1)).findByThreadIdOrderByCreatedAtAsc(1L);
    }
}
