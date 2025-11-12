package com.storefit.catalog_service.Model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Entity
@Table(
    name = "producto",
    indexes = {
        @Index(name = "idx_prod_categoria", columnList = "id_categoria"),
        @Index(name = "idx_prod_modelo", columnList = "modelo")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Producto del catálogo (indumentaria deportiva) con PK compuesta por categoría e id de producto")
public class Producto {

    @EmbeddedId
    @Schema(description = "Clave primaria compuesta (id_categoria, id_producto)")
    private ProductoId id;

    @NotBlank
    @Column(name = "marca", nullable = false, length = 60)
    @Schema(description = "Marca del producto", example = "Adidas")
    private String marca;

    @NotBlank
    @Column(name = "modelo", nullable = false, length = 120)
    @Schema(description = "Modelo o nombre comercial", example = "Ultraboost 5")
    private String modelo;

    @NotBlank
    @Column(name = "color", nullable = false, length = 30)
    @Schema(description = "Color principal del producto", example = "Negro")
    private String color;

    @NotBlank
    @Pattern(regexp = "^(XS|S|M|L|XL)$", message = "La talla debe ser XS, S, M, L o XL")
    @Column(name = "talla", nullable = false, length = 5)
    @Schema(description = "Talla de la prenda", example = "M")
    private String talla; // XS,S,M,L,XL

    @Min(0)
    @Column(name = "precio", nullable = false)
    @Schema(description = "Precio unitario en pesos", example = "29990")
    private Integer precio;

    @Min(0)
    @Column(name = "stock", nullable = false)
    @Schema(description = "Stock disponible", example = "10")
    private Integer stock = 0;

    @Column(name = "image_url", length = 250)
    @Schema(description = "URL de imagen del producto", example = "/img/poleras/polera_sf_01.png")
    private String imageUrl;
}