package com.storefit.support_service.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(name = "mensajes")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class Mensaje {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // Id del usuario que ENVÍA el mensaje (cliente o soporte)
  @Column(name = "sender_user_id", nullable = false)
  private Long senderUserId;

  // Cuando el cliente escribe a SOPORTE (id rol soporte)
  @Column(name = "target_role_id")
  private Integer targetRoleId;

  // Cuando soporte responde a un cliente en específico
  @Column(name = "target_user_id")
  private Long targetUserId;

  @Column(nullable = false, length = 2000)
  private String content;

  // Timestamp de creación (millis)
  @Column(name = "created_at", nullable = false)
  private Long createdAt;

  // ¿Ya lo leyó el receptor?
  @Column(name = "is_read", nullable = false)
  private Boolean read = false;

  // true si es una RESPUESTA (mensaje de soporte)
  @Column(name = "is_response", nullable = false)
  private Boolean isResponse = false;

  // id del mensaje original al que se responde
  @Column(name = "replied_to_id")
  private Long repliedToId;

  // id del hilo (normalmente el id del mensaje original del cliente)
  @Column(name = "thread_id")
  private Long threadId;

  // cuándo fue respondido (timestamp)
  @Column(name = "responded_at")
  private Long respondedAt;
}
