package com.storefit.orders_service.Model;

import java.time.LocalDate;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "compra",
  indexes = {
    @Index(name = "idx_compra_rut", columnList = "rutUsuario"),
    @Index(name = "idx_compra_fecha", columnList = "fechaMillis")
})

@NoArgsConstructor 
@AllArgsConstructor
@Builder

public class Compra {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long idCompra;

  @NotBlank @Column(nullable = false, length = 15)
  private String rutUsuario;

  @Column(nullable = false)
  private LocalDate fechaMillis;
}
