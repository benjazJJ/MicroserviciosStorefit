package com.storefit.support_service.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.storefit.support_service.Model.Mensaje;
import com.storefit.support_service.Model.MensajeConRespuestaDTO;
import com.storefit.support_service.Repository.MensajeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MensajeService {
    // Ajusta si tu rol SOPORTE es otro
    public static final int ROL_SOPORTE = 3;

    private final MensajeRepository repo;

    public List<Mensaje> listarTodos() {
        return repo.findAll();
    }

    public Mensaje obtenerPorId(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mensaje no encontrado"));
    }

    // Cliente → Soporte
    public Mensaje enviarMensajeCliente(Long senderUserId, String contenido) {
        long ahora = Instant.now().toEpochMilli();

        Mensaje mensaje = Mensaje.builder()
                .senderUserId(senderUserId)
                .targetRoleId(ROL_SOPORTE)
                .targetUserId(null)
                .content(contenido.trim())
                .createdAt(ahora)
                .read(false)
                .isResponse(false)
                .repliedToId(null)
                .threadId(null)
                .respondedAt(null)
                .build();

        mensaje = repo.save(mensaje);

        if (mensaje.getThreadId() == null) {
            mensaje.setThreadId(mensaje.getId());
            mensaje = repo.save(mensaje);
        }

        return mensaje;
    }

    // Soporte → Cliente (respuesta)
    public Mensaje responderMensaje(Long idOriginal, Long idUsuarioSoporte, String contenido) {
        Mensaje original = obtenerPorId(idOriginal);
        long ahora = Instant.now().toEpochMilli();

        Mensaje respuesta = Mensaje.builder()
                .senderUserId(idUsuarioSoporte)
                .targetRoleId(null)
                .targetUserId(original.getSenderUserId())
                .content(contenido.trim())
                .createdAt(ahora)
                .read(false)
                .isResponse(true)
                .repliedToId(original.getId())
                .threadId(original.getThreadId() != null ? original.getThreadId() : original.getId())
                .respondedAt(ahora)
                .build();

        original.setRespondedAt(ahora);
        repo.save(original);

        return repo.save(respuesta);
    }

    public Mensaje marcarComoLeido(Long id) {
        Mensaje mensaje = obtenerPorId(id);
        if (!Boolean.TRUE.equals(mensaje.getRead())) {
            mensaje.setRead(true);
            mensaje = repo.save(mensaje);
        }
        return mensaje;
    }

    public void eliminar(Long id) {
        if (!repo.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Mensaje no encontrado");
        }
        repo.deleteById(id);
    }

    public List<MensajeConRespuestaDTO> bandejaSoporte(boolean asc) {
        List<Mensaje> originales = asc
                ? repo.findByTargetRoleIdAndIsResponseFalseOrderByCreatedAtAsc(ROL_SOPORTE)
                : repo.findByTargetRoleIdAndIsResponseFalseOrderByCreatedAtDesc(ROL_SOPORTE);

        List<MensajeConRespuestaDTO> resultado = new ArrayList<>();
        for (Mensaje original : originales) {
            List<Mensaje> respuestas = repo.findByRepliedToIdAndIsResponseTrueOrderByCreatedAtAsc(original.getId());
            Mensaje respuesta = respuestas.isEmpty() ? null : respuestas.get(0);
            resultado.add(new MensajeConRespuestaDTO(original, respuesta));
        }
        return resultado;
    }

    public List<MensajeConRespuestaDTO> bandejaUsuario(Long usuarioId, boolean asc) {
        List<Mensaje> originales = asc
                ? repo.findBySenderUserIdAndIsResponseFalseOrderByCreatedAtAsc(usuarioId)
                : repo.findBySenderUserIdAndIsResponseFalseOrderByCreatedAtDesc(usuarioId);

        List<MensajeConRespuestaDTO> resultado = new ArrayList<>();
        for (Mensaje original : originales) {
            List<Mensaje> respuestas = repo.findByRepliedToIdAndIsResponseTrueOrderByCreatedAtAsc(original.getId());
            Mensaje respuesta = respuestas.isEmpty() ? null : respuestas.get(0);
            resultado.add(new MensajeConRespuestaDTO(original, respuesta));
        }
        return resultado;
    }

    public List<Mensaje> mensajesPorThread(Long threadId) {
        return repo.findByThreadIdOrderByCreatedAtAsc(threadId);
    }
}
