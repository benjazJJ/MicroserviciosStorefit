package com.storefit.support_service.Model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Par de mensaje original del cliente con su respuesta")
public class MensajeConRespuestaDTO {

    @Schema(description = "Mensaje enviado por el cliente")
    private Mensaje clienteMensaje;
    @Schema(description = "Respuesta enviada por soporte")
    private Mensaje respuesta;

}
