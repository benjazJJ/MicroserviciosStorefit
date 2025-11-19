package com.storefit.orders_service.Client;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import com.storefit.orders_service.Model.StockReservaItemDTO;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CatalogClient {
    private final WebClient.Builder webClientBuilder;

    @Value("${catalog-service.url:http://localhost:8081}")
    private String catalogBaseUrl;

    public void reservarStock(List<StockReservaItemDTO> items) {
        String path = "/api/v1/productos/stock/reservar";

        try {
            WebClient client = webClientBuilder
                    .baseUrl(catalogBaseUrl)
                    .build();

            client.post()
                    .uri(path)
                    .bodyValue(items)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError(), r ->
                            r.bodyToMono(String.class)
                             .map(msg -> new ResponseStatusException(
                                     r.statusCode(),
                                     msg != null && !msg.isBlank()
                                             ? msg
                                             : "Error al reservar stock en catalog-service (4xx)"
                             ))
                    )
                    .onStatus(status -> status.is5xxServerError(), r ->
                            r.bodyToMono(String.class)
                             .map(msg -> new ResponseStatusException(
                                     HttpStatus.BAD_GATEWAY,
                                     msg != null && !msg.isBlank()
                                             ? msg
                                             : "Falla interna en catalog-service (5xx)"
                             ))
                    )
                    .toBodilessEntity()
                    .block(); 

        } catch (WebClientResponseException ex) {
            // Errores HTTP no manejados arriba (por ejemplo, timeouts u otros)
            throw new ResponseStatusException(
                    ex.getStatusCode(),
                    ex.getResponseBodyAsString(),
                    ex
            );
        } catch (Exception ex) {
            // Error de red, DNS, servicio caído, etc.
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "No se pudo contactar al catalog-service",
                    ex
            );
        }
    }
}
