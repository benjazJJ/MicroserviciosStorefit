package com.storefit.support_service.Model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
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

  // RUT del usuario que ENVÍA el mensaje (cliente o soporte)
  @Column(name = "rut_remitente", nullable = false, length = 15)
  @Schema(description = "RUT del remitente", example = "12345678-9")
  private String rutRemitente;

  // Cuando el cliente escribe a SOPORTE (id de rol soporte)
  @Column(name = "id_rol_destino")
  @Schema(description = "ID del rol destino (si aplica)", example = "3")
  private Integer idRolDestino;

  // Cuando soporte responde a un cliente específico
  @Column(name = "rut_destino", length = 15)
  @Schema(description = "RUT destino (si aplica)", example = "12345678-9")
  private String rutDestino;

  @Column(name = "contenido", nullable = false, length = 2000)
  @Schema(description = "Contenido del mensaje", example = "Necesito ayuda con mi compra")
  private String contenido;

  // Timestamp de creación (millis)
  @Column(name = "creado_en", nullable = false)
  @Schema(description = "Timestamp de creación (millis)", example = "1717286400000")
  private Long creadoEn;

  // ¿Ya lo leyó el receptor?
  @Column(name = "leido", nullable = false)
  @Schema(description = "Marcado como leído", example = "false")
  private Boolean leido = false;

  // true si es una RESPUESTA (mensaje de soporte)
  @Column(name = "es_respuesta", nullable = false)
  @Schema(description = "Es una respuesta de soporte", example = "false")
  private Boolean esRespuesta = false;

  // id del mensaje original al que se responde
  @Column(name = "responde_a_id")
  @Schema(description = "ID del mensaje original al que se responde")
  private Long respondeAId;

  // id del hilo (normalmente el id del mensaje original del cliente)
  @Column(name = "id_hilo")
  @Schema(description = "ID del hilo de conversación")
  private Long idHilo;

  // cuándo fue respondido (timestamp)
  @Column(name = "respondido_en")
  @Schema(description = "Timestamp de respuesta (millis)")
  private Long respondidoEn;
}
