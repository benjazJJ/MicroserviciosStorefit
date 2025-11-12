package com.storefit.users_service.Model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "registro",
  indexes = { @Index(name = "idx_registro_usuario", columnList = "usuario", unique = true) })
@NoArgsConstructor
@AllArgsConstructor

public class Registro {

  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "rol_id", nullable = false)
  private Long rolId;

  @NotBlank @Column(name = "usuario", nullable = false, length = 60)
  private String usuario;

  @NotBlank @Column(name = "contrasenia", nullable = false, length = 120)
  private String contrasenia;

  @NotBlank @Column(name = "rut", nullable = false, length = 15)
  private String rut;

  @Column(name = "direccion", length = 200)
  private String address;
}
