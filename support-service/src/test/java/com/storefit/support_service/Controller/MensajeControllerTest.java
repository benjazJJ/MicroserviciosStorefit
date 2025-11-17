package com.storefit.support_service.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.storefit.support_service.Model.Mensaje;
import com.storefit.support_service.Model.MensajeConRespuestaDTO;
import com.storefit.support_service.Service.MensajeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = MensajeController.class)
@AutoConfigureMockMvc(addFilters = false)
public class MensajeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MensajeService mensajeService;

    // -------- helpers --------

    private Mensaje crearMensaje(
            Long id,
            Long senderUserId,
            Integer targetRoleId,
            Long targetUserId,
            String content,
            boolean isResponse) {
        Mensaje m = new Mensaje();
        m.setId(id);
        m.setSenderUserId(senderUserId);
        m.setTargetRoleId(targetRoleId);
        m.setTargetUserId(targetUserId);
        m.setContent(content);
        m.setCreatedAt(1763074750001L);
        m.setRead(false);
        m.setIsResponse(isResponse);
        m.setRepliedToId(isResponse ? 1L : null);
        m.setThreadId(1L);
        m.setRespondedAt(null);
        return m;
    }

    // ---------- GET /api/v1/mensajes ----------

    @Test
    void listar_deberiaRetornarListaMensajes() throws Exception {
        Mensaje m1 = crearMensaje(1L, 1L, 3, null, "Hola soporte", false);
        Mensaje m2 = crearMensaje(2L, 2L, 3, null, "Otro mensaje", false);

        when(mensajeService.listarTodos()).thenReturn(List.of(m1, m2));

        mockMvc.perform(get("/api/v1/mensajes"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].content").value("Hola soporte"))
                .andExpect(jsonPath("$[1].id").value(2));
    }

    // ---------- GET /api/v1/mensajes/{id} ----------

    @Test
    void porId_deberiaRetornarMensajePorId() throws Exception {
        Mensaje m1 = crearMensaje(1L, 1L, 3, null, "Hola soporte", false);

        when(mensajeService.obtenerPorId(1L)).thenReturn(m1);

        mockMvc.perform(get("/api/v1/mensajes/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.content").value("Hola soporte"));
    }

    // ---------- DELETE /api/v1/mensajes/{id} ----------

    @Test
    void eliminar_deberiaRetornarNoContent() throws Exception {
        doNothing().when(mensajeService).eliminar(1L);

        mockMvc.perform(delete("/api/v1/mensajes/{id}", 1L))
                .andExpect(status().isNoContent());
    }

    // ---------- PATCH /api/v1/mensajes/{id}/leido ----------

    @Test
    void marcarLeido_deberiaRetornarMensajeActualizado() throws Exception {
        Mensaje leido = crearMensaje(1L, 1L, 3, null, "Hola soporte", false);
        leido.setRead(true);

        when(mensajeService.marcarComoLeido(1L)).thenReturn(leido);

        mockMvc.perform(patch("/api/v1/mensajes/{id}/leido", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.read").value(true));
    }

    // ---------- POST /api/v1/mensajes/cliente ----------

    @Test
    void enviarMensajeCliente_deberiaCrearMensaje() throws Exception {
        Mensaje creado = crearMensaje(1L, 1L, 3, null, "Hola, tengo un problema", false);

        MensajeController.EnviarMensajeRequest request = new MensajeController.EnviarMensajeRequest();
        request.setSenderUserId(1L);
        request.setContent("Hola, tengo un problema");

        when(mensajeService.enviarMensajeCliente(1L, "Hola, tengo un problema"))
                .thenReturn(creado);

        mockMvc.perform(post("/api/v1/mensajes/cliente")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.senderUserId").value(1))
                .andExpect(jsonPath("$.content").value("Hola, tengo un problema"));
    }

    // ---------- POST /api/v1/mensajes/soporte/{originalId}/respuesta ----------

    @Test
    void responderMensaje_deberiaCrearRespuesta() throws Exception {
        Mensaje respuesta = crearMensaje(2L, 9001L, null, 1L,
                "Hola, revisamos tu caso", true);

        MensajeController.ResponderMensajeRequest request = new MensajeController.ResponderMensajeRequest();
        request.setSoporteUserId(9001L);
        request.setContent("Hola, revisamos tu caso");

        when(mensajeService.responderMensaje(1L, 9001L, "Hola, revisamos tu caso"))
                .thenReturn(respuesta);

        mockMvc.perform(post("/api/v1/mensajes/soporte/{originalId}/respuesta", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.senderUserId").value(9001))
                .andExpect(jsonPath("$.targetUserId").value(1))
                .andExpect(jsonPath("$.isResponse").value(true));
    }

    // ---------- GET /api/v1/mensajes/soporte/bandeja ----------

    @Test
    void bandejaSoporte_deberiaRetornarListaMensajesConRespuesta() throws Exception {
        Mensaje original = crearMensaje(1L, 1L, 3, null, "Hola soporte", false);
        Mensaje resp = crearMensaje(2L, 9001L, null, 1L, "Respuesta soporte", true);

        MensajeConRespuestaDTO dto = new MensajeConRespuestaDTO(original, resp);

        when(mensajeService.bandejaSoporte(false))
                .thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/mensajes/soporte/bandeja"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].clienteMensaje.id").value(1))
                .andExpect(jsonPath("$[0].clienteMensaje.content").value("Hola soporte"))
                .andExpect(jsonPath("$[0].respuesta.id").value(2))
                .andExpect(jsonPath("$[0].respuesta.content").value("Respuesta soporte"));
    }

    // ---------- GET /api/v1/mensajes/usuario/{usuarioId}/bandeja ----------

    @Test
    void bandejaUsuario_deberiaRetornarListaMensajesConRespuesta() throws Exception {
        Mensaje original = crearMensaje(1L, 1L, 3, null, "Hola soporte", false);
        Mensaje resp = crearMensaje(2L, 9001L, null, 1L, "Respuesta soporte", true);

        MensajeConRespuestaDTO dto = new MensajeConRespuestaDTO(original, resp);

        when(mensajeService.bandejaUsuario(1L, false))
                .thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/mensajes/usuario/{usuarioId}/bandeja", 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].clienteMensaje.id").value(1))
                .andExpect(jsonPath("$[0].respuesta.id").value(2));
    }

    // ---------- GET /api/v1/mensajes/hilos/{threadId} ----------

    @Test
    void mensajesPorThread_deberiaRetornarListaMensajes() throws Exception {
        Mensaje original = crearMensaje(1L, 1L, 3, null, "Hola soporte", false);
        Mensaje resp = crearMensaje(2L, 9001L, null, 1L, "Respuesta soporte", true);

        when(mensajeService.mensajesPorThread(1L))
                .thenReturn(List.of(original, resp));

        mockMvc.perform(get("/api/v1/mensajes/hilos/{threadId}", 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].isResponse").value(true));
    }
}
