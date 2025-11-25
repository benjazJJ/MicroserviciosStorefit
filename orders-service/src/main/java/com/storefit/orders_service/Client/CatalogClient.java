package com.storefit.orders_service.Client;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
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

                WebClient client = webClientBuilder
                                .baseUrl(catalogBaseUrl)
                                .build();

                try {
                        client.post()
                                        .uri(path)
                                        .bodyValue(items)
                                        .retrieve()
                                        // Si el catálogo responde 4xx, lo propagamos como tal
                                        .onStatus(HttpStatusCode::is4xxClientError, r -> r.bodyToMono(String.class)
                                                        .map(msg -> new ResponseStatusException(
                                                                        r.statusCode(),
                                                                        (msg != null && !msg.isBlank())
                                                                                        ? msg
                                                                                        : "Error al reservar stock en catalog-service (4xx)")))
                                        // Si el catálogo responde 5xx, lo propagamos como 502 (BAD_GATEWAY)
                                        .onStatus(HttpStatusCode::is5xxServerError, r -> r.bodyToMono(String.class)
                                                        .map(msg -> new ResponseStatusException(
                                                                        HttpStatus.BAD_GATEWAY,
                                                                        (msg != null && !msg.isBlank())
                                                                                        ? msg
                                                                                        : "Falla interna en catalog-service (5xx)")))
                                        .toBodilessEntity()
                                        .block();

                } catch (WebClientResponseException ex) {
                        // Errores HTTP que no hayan pasado por onStatus (casos raros)
                        throw new ResponseStatusException(
                                        ex.getStatusCode(),
                                        (ex.getResponseBodyAsString() != null
                                                        && !ex.getResponseBodyAsString().isBlank())
                                                                        ? ex.getResponseBodyAsString()
                                                                        : "Error HTTP al contactar catalog-service",
                                        ex);
                } catch (Exception ex) {
                        throw new ResponseStatusException(
                                        HttpStatus.SERVICE_UNAVAILABLE,
                                        "No se pudo contactar al catalog-service",
                                        ex);
                }
        }
}
