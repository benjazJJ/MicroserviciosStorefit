package com.storefit.users_service.Model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "rol", uniqueConstraints = @UniqueConstraint(columnNames = "nombre_rol"))
@AllArgsConstructor
@NoArgsConstructor


public class Rol {

  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "rol_id")
  private Long rolId;

  @NotBlank
  @Column(name = "nombre_rol", nullable = false, length = 50)
  private String nombreRol; // CLIENTE, ADMIN, SOPORTE
}
