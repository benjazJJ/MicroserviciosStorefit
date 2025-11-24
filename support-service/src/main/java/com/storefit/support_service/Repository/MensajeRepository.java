package com.storefit.support_service.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.storefit.support_service.Model.Mensaje;

public interface MensajeRepository extends JpaRepository<Mensaje, Long> {

    // Bandeja SOPORTE: mensajes originales de clientes (no respuestas)
    List<Mensaje> findByIdRolDestinoAndEsRespuestaFalseOrderByCreadoEnAsc(Integer idRolDestino);
    List<Mensaje> findByIdRolDestinoAndEsRespuestaFalseOrderByCreadoEnDesc(Integer idRolDestino);

    // Bandeja CLIENTE: sus mensajes originales (no respuestas)
    List<Mensaje> findByRutRemitenteAndEsRespuestaFalseOrderByCreadoEnAsc(String rutRemitente);
    List<Mensaje> findByRutRemitenteAndEsRespuestaFalseOrderByCreadoEnDesc(String rutRemitente);

    // Hilo completo
    List<Mensaje> findByIdHiloOrderByCreadoEnAsc(Long idHilo);

    // Respuestas a un mensaje
    List<Mensaje> findByRespondeAIdAndEsRespuestaTrueOrderByCreadoEnAsc(Long respondeAId);
}
