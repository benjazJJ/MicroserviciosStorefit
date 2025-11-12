package com.storefit.catalog_service.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Table(name = "categoria",
  indexes = { @Index(name = "ux_categoria_nombre", columnList = "nombre", unique = true) })

@NoArgsConstructor 
@AllArgsConstructor

public class Categoria {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id_categoria")
  private Long idCategoria;

  @NotBlank @Column(nullable = false, length = 80)
  private String nombre;
}
