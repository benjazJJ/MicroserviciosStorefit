package com.storefit.orders_service.Client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockReservaItemDto {
    private Long idProducto;
    private Integer cantidad;
}
