package com.storefit.catalog_service.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Table(name = "producto",
  indexes = {
    @Index(name = "idx_prod_categoria", columnList = "id_categoria"),
    @Index(name = "idx_prod_modelo", columnList = "modelo")
})

@NoArgsConstructor 
@AllArgsConstructor
@Builder
public class Producto {

  @EmbeddedId
  private ProductoId id;

  @NotBlank @Column(name = "marca", nullable = false, length = 60)
  private String marca;

  @NotBlank @Column(name = "modelo", nullable = false, length = 120)
  private String modelo;

  @NotBlank @Column(name = "color", nullable = false, length = 30)
  private String color;

  @NotBlank @Column(name = "talla", nullable = false, length = 5)
  private String talla; // XS,S,M,L,XL

  @Min(0) @Column(name = "precio", nullable = false)
  private Integer precio;

  @Min(0) @Column(name = "stock", nullable = false)
  private Integer stock = 0;

  @Column(name = "image_url", length = 250)
  private String imageUrl;
}
