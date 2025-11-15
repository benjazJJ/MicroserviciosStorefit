package com.storefit.orders_service.Client;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CatalogClient {

    private final RestTemplate restTemplate;

    @Value("${catalog-service.url:http://localhost:8081}")
    private String catalogBaseUrl;

    public void reservarStock(List<StockReservaItemDto> items) {
        String url = catalogBaseUrl + "/api/v1/productos/stock/reservar";
        try {
            restTemplate.postForEntity(url, items, Void.class);
        } catch (HttpStatusCodeException ex) {
            throw new ResponseStatusException(
                    ex.getStatusCode(),
                    ex.getResponseBodyAsString(),
                    ex
            );
        } catch (RestClientException ex) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "No se pudo contactar al catalog-service",
                    ex
            );
        }
    }
}
