package com.storefit.catalog_service.Model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Embeddable
@NoArgsConstructor 
@AllArgsConstructor
@EqualsAndHashCode
@Data

public class ProductoId implements Serializable {
  @Column(name = "id_categoria")
  private Long idCategoria;

  @Column(name = "id_producto")
  private Long idProducto;
}
