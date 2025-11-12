package com.storefit.orders_service.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Table(name = "compra_detalle",
  indexes = {
    @Index(name = "idx_detalle_compra", columnList = "idCompra"),
    @Index(name = "idx_detalle_producto", columnList = "idProducto")
})

@NoArgsConstructor 
@AllArgsConstructor
@Builder

public class CompraDetalle {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long idDetalle;

  @Column(nullable = false)
  private Long idCompra;

  @Column(nullable = false)
  private Long idProducto;

  @NotBlank @Column(nullable = false, length = 160)
  private String nombreProducto;

  @Min(1) @Column(nullable = false)
  private Integer cantidad;

  @Min(0) @Column(nullable = false)
  private Integer precioUnitario;
}
