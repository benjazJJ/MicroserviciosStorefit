package com.storefit.catalog_service.Model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Schema(description = "Clave primaria compuesta para Producto (id_categoria + id_producto)")
public class ProductoId implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "id_categoria", nullable = false)
    @Schema(description = "ID de la categoría", example = "1")
    private Long idCategoria;

    @Column(name = "id_producto", nullable = false)
    @Schema(description = "ID del producto dentro de la categoría", example = "101")
    private Long idProducto;
}