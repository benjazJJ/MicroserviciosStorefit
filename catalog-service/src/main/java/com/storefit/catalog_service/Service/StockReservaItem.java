package com.storefit.catalog_service.Service;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Item de reserva de stock por producto")
public class StockReservaItem {

    @Schema(description = "ID interno del producto (id_producto)", example = "1001")
    private Long idProducto;

    @Schema(description = "Cantidad que se desea descontar del stock", example = "2")
    private Integer cantidad;
}
