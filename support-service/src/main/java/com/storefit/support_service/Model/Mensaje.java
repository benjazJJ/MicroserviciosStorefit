package com.storefit.support_service.Model;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Mensaje de soporte entre usuario y equipo de soporte")
public class Mensaje {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Schema(description = "ID del mensaje", example = "100")
  private Long id;

  // Id del usuario que ENVÍA el mensaje (cliente o soporte)
  @Column(name = "sender_user_id", nullable = false)
  @Schema(description = "ID del usuario que envía", example = "5001")
  private Long senderUserId;

  // Cuando el cliente escribe a SOPORTE (id rol soporte)
  @Column(name = "target_role_id")
  @Schema(description = "Rol destino (si aplica)", example = "3")
  private Integer targetRoleId;

  // Cuando soporte responde a un cliente en específico
  @Column(name = "target_user_id")
  @Schema(description = "Usuario destino (si aplica)", example = "7001")
  private Long targetUserId;

  @Column(nullable = false, length = 2000)
  @Schema(description = "Contenido del mensaje", example = "Necesito ayuda con mi compra")
  private String content;

  // Timestamp de creación (millis)
  @Column(name = "created_at", nullable = false)
  @Schema(description = "Timestamp de creación (millis)", example = "1717286400000")
  private Long createdAt;

  // ¿Ya lo leyó el receptor?
  @Column(name = "is_read", nullable = false)
  @Schema(description = "Marcado como leído", example = "false")
  private Boolean read = false;

  // true si es una RESPUESTA (mensaje de soporte)
  @Column(name = "is_response", nullable = false)
  @Schema(description = "Es una respuesta de soporte", example = "false")
  private Boolean isResponse = false;

  // id del mensaje original al que se responde
  @Column(name = "replied_to_id")
  @Schema(description = "ID del mensaje original al que se responde")
  private Long repliedToId;

  // id del hilo (normalmente el id del mensaje original del cliente)
  @Column(name = "thread_id")
  @Schema(description = "ID del hilo de conversación")
  private Long threadId;

  // cuándo fue respondido (timestamp)
  @Column(name = "responded_at")
  @Schema(description = "Timestamp de respuesta (millis)")
  private Long respondedAt;
}
