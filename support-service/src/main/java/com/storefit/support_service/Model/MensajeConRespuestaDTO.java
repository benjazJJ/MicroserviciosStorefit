package com.storefit.support_service.Model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class MensajeConRespuestaDTO {

    private Mensaje clienteMensaje;
    private Mensaje respuesta;

}
