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
        String rutDotted = toDottedRut(rut);
        String path = "/api/v1/usuarios/" + rutDotted;
        try {
            WebClient client = webClientBuilder.baseUrl(usersBaseUrl).build();
            return client.get()
                    .uri(path)
                    .header("X-User-Rut", rutDotted)
                    .header("X-User-Rol", "ADMIN") // ajusta si quieres propagar otro rol
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError(), r -> r.bodyToMono(String.class)
                            .map(msg -> new ResponseStatusException(
                                    HttpStatus.NOT_FOUND,
                                    (msg != null && !msg.isBlank()) ? msg : "Usuario no encontrado")))
                    .onStatus(status -> status.is5xxServerError(), r -> r.bodyToMono(String.class)
                            .map(msg -> new ResponseStatusException(
                                    HttpStatus.BAD_GATEWAY,
                                    (msg != null && !msg.isBlank()) ? msg : "Error en users-service")))
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
        String rutDotted = toDottedRut(rut);
        String path = "/api/v1/usuarios/" + rutDotted;
        try {
            WebClient client = webClientBuilder.baseUrl(usersBaseUrl).build();
            client.get()
                    .uri(path)
                    .header("X-User-Rut", rutDotted)
                    .header("X-User-Rol", "ADMIN") // ajusta si quieres propagar otro rol
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError(), r -> r.bodyToMono(String.class)
                            .map(msg -> new ResponseStatusException(
                                    HttpStatus.NOT_FOUND,
                                    (msg != null && !msg.isBlank()) ? msg : "Usuario no encontrado")))
                    .onStatus(status -> status.is5xxServerError(), r -> r.bodyToMono(String.class)
                            .map(msg -> new ResponseStatusException(
                                    HttpStatus.BAD_GATEWAY,
                                    (msg != null && !msg.isBlank()) ? msg : "Error en users-service")))
                    .toBodilessEntity()
                    .block();
        } catch (WebClientResponseException ex) {
            throw new ResponseStatusException(ex.getStatusCode(), ex.getResponseBodyAsString(), ex);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "No se pudo contactar users-service", ex);
        }
    }

    // Normaliza a formato con puntos y guión para cumplir con RutUtils
    private String toDottedRut(String rut) {
        String clean = rut.replace(".", "").replace("-", "").trim();
        if (clean.length() < 2)
            return rut;
        String dv = clean.substring(clean.length() - 1);
        String body = clean.substring(0, clean.length() - 1);
        StringBuilder sb = new StringBuilder(body).reverse();
        StringBuilder dotted = new StringBuilder();
        for (int i = 0; i < sb.length(); i++) {
            if (i > 0 && i % 3 == 0)
                dotted.append('.');
            dotted.append(sb.charAt(i));
        }
        return dotted.reverse().append('-').append(dv).toString();
    }
}