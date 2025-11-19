package com.storefit.orders_service.Client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import com.storefit.orders_service.Model.UsuarioDTO;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UsersClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${users-service.url:http://localhost:8084}")
    private String usersBaseUrl;

    // GET /api/v1/usuarios/{rut}: devuelve el usuario
    public UsuarioDTO obtenerUsuarioPorRut(String rut) {
        String path = "/api/v1/usuarios/" + rut;
        try {
            WebClient client = webClientBuilder.baseUrl(usersBaseUrl).build();
            return client.get()
                    .uri(path)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError(), r ->
                            r.bodyToMono(String.class)
                             .map(msg -> new ResponseStatusException(
                                     HttpStatus.NOT_FOUND,
                                     (msg != null && !msg.isBlank()) ? msg : "Usuario no encontrado"
                             ))
                    )
                    .onStatus(status -> status.is5xxServerError(), r ->
                            r.bodyToMono(String.class)
                             .map(msg -> new ResponseStatusException(
                                     HttpStatus.BAD_GATEWAY,
                                     (msg != null && !msg.isBlank()) ? msg : "Error en users-service"
                             ))
                    )
                    .bodyToMono(UsuarioDTO.class)
                    .block();
        } catch (WebClientResponseException ex) {
            throw new ResponseStatusException(ex.getStatusCode(), ex.getResponseBodyAsString(), ex);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "No se pudo contactar users-service", ex);
        }
    }

    // Validación simple: solo asegura 2xx
    public void validarUsuarioExistePorRut(String rut) {
        String path = "/api/v1/usuarios/" + rut;
        try {
            WebClient client = webClientBuilder.baseUrl(usersBaseUrl).build();
            client.get()
                    .uri(path)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError(), r ->
                            r.bodyToMono(String.class)
                             .map(msg -> new ResponseStatusException(
                                     HttpStatus.NOT_FOUND,
                                     (msg != null && !msg.isBlank()) ? msg : "Usuario no encontrado"
                             ))
                    )
                    .onStatus(status -> status.is5xxServerError(), r ->
                            r.bodyToMono(String.class)
                             .map(msg -> new ResponseStatusException(
                                     HttpStatus.BAD_GATEWAY,
                                     (msg != null && !msg.isBlank()) ? msg : "Error en users-service"
                             ))
                    )
                    .toBodilessEntity()
                    .block();
        } catch (WebClientResponseException ex) {
            throw new ResponseStatusException(ex.getStatusCode(), ex.getResponseBodyAsString(), ex);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "No se pudo contactar users-service", ex);
        }
    }
}
