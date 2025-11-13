package com.storefit.support_service.Controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.storefit.support_service.Model.Mensaje;
import com.storefit.support_service.Model.MensajeConRespuestaDTO;
import com.storefit.support_service.Service.MensajeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/mensajes")
@RequiredArgsConstructor
@Tag(name = "Mensajes de soporte", description = "API para gestionar mensajes de soporte cliente ↔ soporte")
public class MensajeController {

    private final MensajeService service;

    // ---------- CRUD básico ----------

    @GetMapping
    @Operation(summary = "Listar todos los mensajes")
    public List<Mensaje> listar() {
        return service.listarTodos();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un mensaje por id")
    public Mensaje porId(@PathVariable Long id) {
        return service.obtenerPorId(id);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un mensaje")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        service.eliminar(id);
    }

    @PatchMapping("/{id}/leido")
    @Operation(summary = "Marcar un mensaje como leído")
    public Mensaje marcarLeido(@PathVariable Long id) {
        return service.marcarComoLeido(id);
    }

    // ---------- Operaciones de negocio ----------

    @PostMapping("/cliente")
    @Operation(summary = "Cliente envía mensaje a soporte")
    public ResponseEntity<Mensaje> enviarMensajeCliente(
            @RequestBody EnviarMensajeRequest request) {
        Mensaje creado = service.enviarMensajeCliente(
                request.getSenderUserId(),
                request.getContent());
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PostMapping("/soporte/{originalId}/respuesta")
    @Operation(summary = "Soporte responde a mensaje de cliente")
    public ResponseEntity<Mensaje> responderMensaje(
            @PathVariable Long originalId,
            @RequestBody ResponderMensajeRequest request) {
        Mensaje respuesta = service.responderMensaje(
                originalId,
                request.getSoporteUserId(),
                request.getContent());
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    @GetMapping("/soporte/bandeja")
    @Operation(summary = "Bandeja de soporte")
    public List<MensajeConRespuestaDTO> bandejaSoporte(
            @RequestParam(defaultValue = "false") boolean asc) {
        return service.bandejaSoporte(asc);
    }

    @GetMapping("/usuario/{usuarioId}/bandeja")
    @Operation(summary = "Bandeja de un cliente")
    public List<MensajeConRespuestaDTO> bandejaUsuario(
            @PathVariable Long usuarioId,
            @RequestParam(defaultValue = "false") boolean asc) {
        return service.bandejaUsuario(usuarioId, asc);
    }

    @GetMapping("/hilos/{threadId}")
    @Operation(summary = "Obtener un hilo completo")
    public List<Mensaje> mensajesPorThread(@PathVariable Long threadId) {
        return service.mensajesPorThread(threadId);
    }

    // ---------- DTOs de entrada ----------

    @Data
    public static class EnviarMensajeRequest {
        private Long senderUserId;
        private String content;
    }

    @Data
    public static class ResponderMensajeRequest {
        private Long soporteUserId;
        private String content;
    }
}
