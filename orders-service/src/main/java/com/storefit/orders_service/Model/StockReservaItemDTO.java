package com.storefit.orders_service.Model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockReservaItemDTO {
    private Long idProducto;
    private Integer cantidad;
}

