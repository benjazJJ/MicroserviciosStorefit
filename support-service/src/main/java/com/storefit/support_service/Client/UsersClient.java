package com.storefit.support_service.Client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

@Component
public class UsersClient {

    @Value("${users-service.url:http://localhost:8084}")
    private String usersBaseUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    // Valida que exista el usuario por RUT en users-service enviando los headers
    // requeridos
    public void validarUsuarioExistePorRut(String rut) {
        String url = usersBaseUrl + "/api/v1/usuarios/" + rut;
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-User-Rut", rut); // el propio rut
            headers.set("X-User-Rol", "CLIENTE"); // rol de cliente para esta consulta
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<String> resp = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            if (!resp.getStatusCode().is2xxSuccessful()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado");
            }
        } catch (HttpStatusCodeException ex) {
            if (ex.getStatusCode().value() == 404) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado");
            }
            // Propaga claramente los 401/403 del users-service
            if (ex.getStatusCode().value() == 401 || ex.getStatusCode().value() == 403) {
                throw new ResponseStatusException(ex.getStatusCode(), "No autorizado en users-service", ex);
            }
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Error en users-service: " + ex.getStatusCode().value(), ex);
        } catch (RestClientException ex) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "No se pudo contactar users-service", ex);
        }
    }
}
