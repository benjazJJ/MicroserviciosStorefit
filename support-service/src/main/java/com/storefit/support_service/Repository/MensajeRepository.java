package com.storefit.support_service.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.storefit.support_service.Model.Mensaje;

public interface MensajeRepository extends JpaRepository<Mensaje, Long> {

    // Bandeja SOPORTE: mensajes originales de clientes (no respuestas)
    List<Mensaje> findByTargetRoleIdAndIsResponseFalseOrderByCreatedAtAsc(Integer targetRoleId);
    List<Mensaje> findByTargetRoleIdAndIsResponseFalseOrderByCreatedAtDesc(Integer targetRoleId);

    // Bandeja CLIENTE: sus mensajes originales (no respuestas)
    List<Mensaje> findBySenderUserIdAndIsResponseFalseOrderByCreatedAtAsc(Long senderUserId);
    List<Mensaje> findBySenderUserIdAndIsResponseFalseOrderByCreatedAtDesc(Long senderUserId);

    // Hilo completo
    List<Mensaje> findByThreadIdOrderByCreatedAtAsc(Long threadId);

    // Respuestas a un mensaje
    List<Mensaje> findByRepliedToIdAndIsResponseTrueOrderByCreatedAtAsc(Long repliedToId);
}
