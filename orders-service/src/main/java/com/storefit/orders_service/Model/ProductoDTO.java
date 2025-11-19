package com.storefit.orders_service.Model;

import lombok.Data;

@Data
public class ProductoDTO {
    private Id id;            // { idCategoria, idProducto }
    private String marca;
    private String modelo;
    private String color;
    private String talla;
    private Integer precio;
    private Integer stock;
    private String imageUrl;

    @Data
    public static class Id {
        private Long idCategoria;
        private Long idProducto;
    }
}
