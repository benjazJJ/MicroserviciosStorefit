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
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = Mensaje.class)))
    })
    public List<Mensaje> listar() {
        return service.listarTodos();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un mensaje por id")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Encontrado",
            content = @Content(schema = @Schema(implementation = Mensaje.class))),
        @ApiResponse(responseCode = "404", description = "No encontrado")
    })
    public Mensaje porId(@PathVariable Long id) {
        return service.obtenerPorId(id);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un mensaje")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Eliminado"),
        @ApiResponse(responseCode = "404", description = "No encontrado")
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        service.eliminar(id);
    }

    @PatchMapping("/{id}/leido")
    @Operation(summary = "Marcar un mensaje como leído")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Actualizado",
            content = @Content(schema = @Schema(implementation = Mensaje.class))),
        @ApiResponse(responseCode = "404", description = "No encontrado")
    })
    public Mensaje marcarLeido(@PathVariable Long id) {
        return service.marcarComoLeido(id);
    }

    // ---------- Operaciones de negocio ----------

    @PostMapping("/cliente")
    @Operation(summary = "Cliente envía mensaje a soporte")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Creado",
            content = @Content(schema = @Schema(implementation = Mensaje.class))),
        @ApiResponse(responseCode = "400", description = "Datos inválidos")
    })
    public ResponseEntity<Mensaje> enviarMensajeCliente(
            @RequestBody EnviarMensajeRequest request) {
        Mensaje creado = service.enviarMensajeCliente(
                request.getSenderUserId(),
                request.getContent());
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PostMapping("/soporte/{originalId}/respuesta")
    @Operation(summary = "Soporte responde a mensaje de cliente")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Creado",
            content = @Content(schema = @Schema(implementation = Mensaje.class))),
        @ApiResponse(responseCode = "404", description = "Mensaje original no encontrado")
    })
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
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = MensajeConRespuestaDTO.class)))
    })
    public List<MensajeConRespuestaDTO> bandejaSoporte(
            @RequestParam(defaultValue = "false") boolean asc) {
        return service.bandejaSoporte(asc);
    }

    @GetMapping("/usuario/{usuarioId}/bandeja")
    @Operation(summary = "Bandeja de un cliente")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = MensajeConRespuestaDTO.class)))
    })
    public List<MensajeConRespuestaDTO> bandejaUsuario(
            @PathVariable Long usuarioId,
            @RequestParam(defaultValue = "false") boolean asc) {
        return service.bandejaUsuario(usuarioId, asc);
    }

    @GetMapping("/hilos/{threadId}")
    @Operation(summary = "Obtener un hilo completo")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = Mensaje.class)))
    })
    public List<Mensaje> mensajesPorThread(@PathVariable Long threadId) {
        return service.mensajesPorThread(threadId);
    }

    // ---------- DTOs de entrada ----------

    @Data
    @Schema(description = "Solicitud de envío de mensaje de cliente a soporte")
    public static class EnviarMensajeRequest {
        @Schema(description = "ID del usuario que envía", example = "5001")
        private Long senderUserId;
        @Schema(description = "Contenido", example = "Tengo un problema con mi pedido")
        private String content;
    }

    @Data
    @Schema(description = "Solicitud de respuesta de soporte a un mensaje")
    public static class ResponderMensajeRequest {
        @Schema(description = "ID del usuario de soporte", example = "9001")
        private Long soporteUserId;
        @Schema(description = "Contenido de la respuesta", example = "Te contactaremos a la brevedad")
        private String content;
    }
}
