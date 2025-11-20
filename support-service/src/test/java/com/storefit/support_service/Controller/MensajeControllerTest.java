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
            String rutRemitente,
            Integer idRolDestino,
            String rutDestino,
            String contenido,
            boolean esRespuesta) {
        Mensaje m = new Mensaje();
        m.setId(id);
        m.setRutRemitente(rutRemitente);
        m.setIdRolDestino(idRolDestino);
        m.setRutDestino(rutDestino);
        m.setContenido(contenido);
        m.setCreadoEn(1763074750001L);
        m.setLeido(false);
        m.setEsRespuesta(esRespuesta);
        m.setRespondeAId(esRespuesta ? 1L : null);
        m.setIdHilo(1L);
        m.setRespondidoEn(null);
        return m;
    }

    // ---------- GET /api/v1/mensajes ----------

    @Test
    void listar_deberiaRetornarListaMensajes() throws Exception {
        Mensaje m1 = crearMensaje(1L, "11111111-1", 3, null, "Hola soporte", false);
        Mensaje m2 = crearMensaje(2L, "22222222-2", 3, null, "Otro mensaje", false);

        when(mensajeService.listarTodos()).thenReturn(List.of(m1, m2));

        mockMvc.perform(get("/api/v1/mensajes"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].contenido").value("Hola soporte"))
                .andExpect(jsonPath("$[1].id").value(2));
    }

    // ---------- GET /api/v1/mensajes/{id} ----------

    @Test
    void porId_deberiaRetornarMensajePorId() throws Exception {
        Mensaje m1 = crearMensaje(1L, "11111111-1", 3, null, "Hola soporte", false);

        when(mensajeService.obtenerPorId(1L)).thenReturn(m1);

        mockMvc.perform(get("/api/v1/mensajes/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.contenido").value("Hola soporte"));
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
        Mensaje leido = crearMensaje(1L, "11111111-1", 3, null, "Hola soporte", false);
        leido.setLeido(true);

        when(mensajeService.marcarComoLeido(1L)).thenReturn(leido);

        mockMvc.perform(patch("/api/v1/mensajes/{id}/leido", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.leido").value(true));
    }

    // ---------- POST /api/v1/mensajes/cliente ----------

    @Test
    void enviarMensajeCliente_deberiaCrearMensaje() throws Exception {
        Mensaje creado = crearMensaje(1L, "12345678-9", 3, null, "Hola, tengo un problema", false);

        MensajeController.EnviarMensajeRequest request = new MensajeController.EnviarMensajeRequest();
        request.setRutRemitente("12345678-9");
        request.setContenido("Hola, tengo un problema");

        when(mensajeService.enviarMensajeCliente("12345678-9", "Hola, tengo un problema"))
                .thenReturn(creado);

        mockMvc.perform(post("/api/v1/mensajes/cliente")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.rutRemitente").value("12345678-9"))
                .andExpect(jsonPath("$.contenido").value("Hola, tengo un problema"));
    }

    // ---------- POST /api/v1/mensajes/soporte/{originalId}/respuesta ----------

    @Test
    void responderMensaje_deberiaCrearRespuesta() throws Exception {
        Mensaje respuesta = crearMensaje(2L, "11111111-1", null, "12345678-9",
                "Hola, revisamos tu caso", true);

        MensajeController.ResponderMensajeRequest request = new MensajeController.ResponderMensajeRequest();
        request.setRutSoporte("11111111-1");
        request.setContenido("Hola, revisamos tu caso");

        when(mensajeService.responderMensaje(1L, "11111111-1", "Hola, revisamos tu caso"))
                .thenReturn(respuesta);

        mockMvc.perform(post("/api/v1/mensajes/soporte/{originalId}/respuesta", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.rutRemitente").value("11111111-1"))
                .andExpect(jsonPath("$.rutDestino").value("12345678-9"))
                .andExpect(jsonPath("$.esRespuesta").value(true));
    }

    // ---------- GET /api/v1/mensajes/soporte/bandeja ----------

    @Test
    void bandejaSoporte_deberiaRetornarListaMensajesConRespuesta() throws Exception {
        Mensaje original = crearMensaje(1L, "12345678-9", 3, null, "Hola soporte", false);
        Mensaje resp = crearMensaje(2L, "11111111-1", null, "12345678-9", "Respuesta soporte", true);

        MensajeConRespuestaDTO dto = new MensajeConRespuestaDTO(original, resp);

        when(mensajeService.bandejaSoporte(false))
                .thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/mensajes/soporte/bandeja"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].clienteMensaje.id").value(1))
                .andExpect(jsonPath("$[0].clienteMensaje.contenido").value("Hola soporte"))
                .andExpect(jsonPath("$[0].respuesta.id").value(2))
                .andExpect(jsonPath("$[0].respuesta.contenido").value("Respuesta soporte"));
    }

    // ---------- GET /api/v1/mensajes/usuario/{rut}/bandeja ----------

    @Test
    void bandejaUsuario_deberiaRetornarListaMensajesConRespuesta() throws Exception {
        Mensaje original = crearMensaje(1L, "12345678-9", 3, null, "Hola soporte", false);
        Mensaje resp = crearMensaje(2L, "11111111-1", null, "12345678-9", "Respuesta soporte", true);

        MensajeConRespuestaDTO dto = new MensajeConRespuestaDTO(original, resp);

        when(mensajeService.bandejaUsuario("12345678-9", false))
                .thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/mensajes/usuario/{rut}/bandeja", "12345678-9"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].clienteMensaje.id").value(1))
                .andExpect(jsonPath("$[0].respuesta.id").value(2));
    }

    // ---------- GET /api/v1/mensajes/hilos/{threadId} ----------

    @Test
    void mensajesPorThread_deberiaRetornarListaMensajes() throws Exception {
        Mensaje original = crearMensaje(1L, "12345678-9", 3, null, "Hola soporte", false);
        Mensaje resp = crearMensaje(2L, "11111111-1", null, "12345678-9", "Respuesta soporte", true);

        when(mensajeService.mensajesPorThread(1L))
                .thenReturn(List.of(original, resp));

        mockMvc.perform(get("/api/v1/mensajes/hilos/{idHilo}", 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].esRespuesta").value(true));
    }
}
