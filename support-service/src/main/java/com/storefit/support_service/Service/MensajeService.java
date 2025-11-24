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
import com.storefit.support_service.Client.UsersClient;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MensajeService {
    // Ajusta si tu rol SOPORTE es otro
    public static final int ROL_SOPORTE = 3;

    private final MensajeRepository repo;
    private final UsersClient usersClient;

    public List<Mensaje> listarTodos() {
        return repo.findAll();
    }

    public Mensaje obtenerPorId(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mensaje no encontrado"));
    }

    // Cliente → Soporte (ahora usando RUT)
    public Mensaje enviarMensajeCliente(String rutRemitente, String contenido) {
        if (rutRemitente == null || rutRemitente.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debe indicar el RUT del remitente");
        }

        // Validar que el usuario exista en users-service
        usersClient.validarUsuarioExistePorRut(rutRemitente.trim());

        long ahora = Instant.now().toEpochMilli();

        Mensaje mensaje = Mensaje.builder()
                .rutRemitente(rutRemitente.trim())
                .idRolDestino(ROL_SOPORTE)
                .rutDestino(null)
                .contenido(contenido == null ? null : contenido.trim())
                .creadoEn(ahora)
                .leido(false)
                .esRespuesta(false)
                .respondeAId(null)
                .idHilo(null)
                .respondidoEn(null)
                .build();

        mensaje = repo.save(mensaje);

        if (mensaje.getIdHilo() == null) {
            mensaje.setIdHilo(mensaje.getId());
            mensaje = repo.save(mensaje);
        }

        return mensaje;
    }

    // Soporte → Cliente (respuesta) usando RUT
    public Mensaje responderMensaje(Long idOriginal, String rutSoporte, String contenido) {
        if (rutSoporte == null || rutSoporte.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debe indicar el RUT del usuario de soporte");
        }
        // Validar que el usuario de soporte exista
        usersClient.validarUsuarioExistePorRut(rutSoporte.trim());

        Mensaje original = obtenerPorId(idOriginal);
        long ahora = Instant.now().toEpochMilli();

        Mensaje respuesta = Mensaje.builder()
                .rutRemitente(rutSoporte.trim())
                .idRolDestino(null)
                .rutDestino(original.getRutRemitente())
                .contenido(contenido == null ? null : contenido.trim())
                .creadoEn(ahora)
                .leido(false)
                .esRespuesta(true)
                .respondeAId(original.getId())
                .idHilo(original.getIdHilo() != null ? original.getIdHilo() : original.getId())
                .respondidoEn(ahora)
                .build();

        original.setRespondidoEn(ahora);
        repo.save(original);

        return repo.save(respuesta);
    }

    public Mensaje marcarComoLeido(Long id) {
        Mensaje mensaje = obtenerPorId(id);
        if (!Boolean.TRUE.equals(mensaje.getLeido())) {
            mensaje.setLeido(true);
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
                ? repo.findByIdRolDestinoAndEsRespuestaFalseOrderByCreadoEnAsc(ROL_SOPORTE)
                : repo.findByIdRolDestinoAndEsRespuestaFalseOrderByCreadoEnDesc(ROL_SOPORTE);

        List<MensajeConRespuestaDTO> resultado = new ArrayList<>();
        for (Mensaje original : originales) {
            List<Mensaje> respuestas = repo.findByRespondeAIdAndEsRespuestaTrueOrderByCreadoEnAsc(original.getId());
            Mensaje respuesta = respuestas.isEmpty() ? null : respuestas.get(0);
            resultado.add(new MensajeConRespuestaDTO(original, respuesta));
        }
        return resultado;
    }

    public List<MensajeConRespuestaDTO> bandejaUsuario(String rut, boolean asc) {
        List<Mensaje> originales = asc
                ? repo.findByRutRemitenteAndEsRespuestaFalseOrderByCreadoEnAsc(rut)
                : repo.findByRutRemitenteAndEsRespuestaFalseOrderByCreadoEnDesc(rut);

        List<MensajeConRespuestaDTO> resultado = new ArrayList<>();
        for (Mensaje original : originales) {
            List<Mensaje> respuestas = repo.findByRespondeAIdAndEsRespuestaTrueOrderByCreadoEnAsc(original.getId());
            Mensaje respuesta = respuestas.isEmpty() ? null : respuestas.get(0);
            resultado.add(new MensajeConRespuestaDTO(original, respuesta));
        }
        return resultado;
    }

    public List<Mensaje> mensajesPorThread(Long idHilo) {
        return repo.findByIdHiloOrderByCreadoEnAsc(idHilo);
    }
}
