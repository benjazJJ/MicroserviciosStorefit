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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.storefit.support_service.Model.Mensaje;
import com.storefit.support_service.Model.MensajeConRespuestaDTO;
import com.storefit.support_service.Service.MensajeService;
import com.storefit.support_service.security.Authorization;
import com.storefit.support_service.security.RequestUser;

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
    public List<Mensaje> listar(
            @RequestHeader("X-User-Rut") String headerRut,
            @RequestHeader("X-User-Rol") String headerRol) {
        RequestUser user = Authorization.fromHeaders(headerRut, headerRol);
        Authorization.requireSupport(user);
        return service.listarTodos();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un mensaje por id")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Encontrado",
            content = @Content(schema = @Schema(implementation = Mensaje.class))),
        @ApiResponse(responseCode = "404", description = "No encontrado")
    })
    public Mensaje porId(@PathVariable Long id,
                         @RequestHeader("X-User-Rut") String headerRut,
                         @RequestHeader("X-User-Rol") String headerRol) {
        Mensaje m = service.obtenerPorId(id);
        RequestUser user = Authorization.fromHeaders(headerRut, headerRol);
        boolean participante = (m.getRutRemitente() != null && m.getRutRemitente().equalsIgnoreCase(user.getRut()))
                || (m.getRutDestino() != null && m.getRutDestino().equalsIgnoreCase(user.getRut()));
        if (!(user.isSoporte() || participante)) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN,
                    "No autorizado a ver este mensaje");
        }
        return m;
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un mensaje")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Eliminado"),
        @ApiResponse(responseCode = "404", description = "No encontrado")
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id,
                         @RequestHeader("X-User-Rut") String headerRut,
                         @RequestHeader("X-User-Rol") String headerRol) {
        RequestUser user = Authorization.fromHeaders(headerRut, headerRol);
        Authorization.requireSupport(user);
        service.eliminar(id);
    }

    @PatchMapping("/{id}/leido")
    @Operation(summary = "Marcar un mensaje como leído")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Actualizado",
            content = @Content(schema = @Schema(implementation = Mensaje.class))),
        @ApiResponse(responseCode = "404", description = "No encontrado")
    })
    public Mensaje marcarLeido(@PathVariable Long id,
                               @RequestHeader("X-User-Rut") String headerRut,
                               @RequestHeader("X-User-Rol") String headerRol) {
        RequestUser user = Authorization.fromHeaders(headerRut, headerRol);
        Mensaje m = service.obtenerPorId(id);
        boolean participante = (m.getRutRemitente() != null && m.getRutRemitente().equalsIgnoreCase(user.getRut()))
                || (m.getRutDestino() != null && m.getRutDestino().equalsIgnoreCase(user.getRut()));
        if (!(user.isSoporte() || participante)) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN,
                    "No autorizado a marcar este mensaje");
        }
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
            @RequestHeader("X-User-Rut") String headerRut,
            @RequestHeader("X-User-Rol") String headerRol,
            @RequestBody EnviarMensajeRequest request) {
        RequestUser user = Authorization.fromHeaders(headerRut, headerRol);
        Authorization.requireCliente(user);
        if (request.getRutRemitente() == null || !user.getRut().equalsIgnoreCase(request.getRutRemitente())) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN,
                    "No puedes enviar mensajes usando otro RUT");
        }
        Mensaje creado = service.enviarMensajeCliente(
                request.getRutRemitente(),
                request.getContenido());
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
            @RequestHeader("X-User-Rut") String headerRut,
            @RequestHeader("X-User-Rol") String headerRol,
            @RequestBody ResponderMensajeRequest request) {
        RequestUser user = Authorization.fromHeaders(headerRut, headerRol);
        Authorization.requireSupport(user);
        if (request.getRutSoporte() == null || !user.getRut().equalsIgnoreCase(request.getRutSoporte())) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN,
                    "No puedes responder como otro usuario de soporte");
        }
        Mensaje respuesta = service.responderMensaje(
                originalId,
                request.getRutSoporte(),
                request.getContenido());
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    @GetMapping("/soporte/bandeja")
    @Operation(summary = "Bandeja de soporte")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = MensajeConRespuestaDTO.class)))
    })
    public List<MensajeConRespuestaDTO> bandejaSoporte(
            @RequestHeader("X-User-Rut") String headerRut,
            @RequestHeader("X-User-Rol") String headerRol,
            @RequestParam(defaultValue = "false") boolean asc) {
        RequestUser user = Authorization.fromHeaders(headerRut, headerRol);
        Authorization.requireSupport(user);
        return service.bandejaSoporte(asc);
    }

    @GetMapping("/usuario/{rut}/bandeja")
    @Operation(summary = "Bandeja de un cliente")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = MensajeConRespuestaDTO.class)))
    })
    public List<MensajeConRespuestaDTO> bandejaUsuario(
            @PathVariable String rut,
            @RequestHeader("X-User-Rut") String headerRut,
            @RequestHeader("X-User-Rol") String headerRol,
            @RequestParam(defaultValue = "false") boolean asc) {
        RequestUser user = Authorization.fromHeaders(headerRut, headerRol);
        if (!(user.isSoporte() || user.getRut().equalsIgnoreCase(rut))) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN,
                    "No autorizado a ver esta bandeja");
        }
        return service.bandejaUsuario(rut, asc);
    }

    @GetMapping("/hilos/{idHilo}")
    @Operation(summary = "Obtener un hilo completo")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = Mensaje.class)))
    })
    public List<Mensaje> mensajesPorThread(@PathVariable Long idHilo,
                                           @RequestHeader("X-User-Rut") String headerRut,
                                           @RequestHeader("X-User-Rol") String headerRol) {
        List<Mensaje> mensajes = service.mensajesPorThread(idHilo);
        RequestUser user = Authorization.fromHeaders(headerRut, headerRol);
        boolean participa = mensajes.stream().anyMatch(m ->
                (m.getRutRemitente() != null && m.getRutRemitente().equalsIgnoreCase(user.getRut())) ||
                (m.getRutDestino() != null && m.getRutDestino().equalsIgnoreCase(user.getRut()))
        );
        if (!(user.isSoporte() || participa)) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN,
                    "No autorizado a ver este hilo");
        }
        return mensajes;
    }

    // ---------- DTOs de entrada ----------

    @Data
    @Schema(description = "Solicitud de envío de mensaje de cliente a soporte")
    public static class EnviarMensajeRequest {
        @Schema(description = "RUT del remitente", example = "12345678-9")
        private String rutRemitente;
        @Schema(description = "Contenido", example = "Tengo un problema con mi pedido")
        private String contenido;
    }

    @Data
    @Schema(description = "Solicitud de respuesta de soporte a un mensaje")
    public static class ResponderMensajeRequest {
        @Schema(description = "RUT del usuario de soporte", example = "11111111-1")
        private String rutSoporte;
        @Schema(description = "Contenido de la respuesta", example = "Te contactaremos a la brevedad")
        private String contenido;
    }
}
