package com.storefit.users_service.Model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Data;

@Entity
@Table(name = "rol", uniqueConstraints = @UniqueConstraint(columnNames = "nombre_rol"))
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder


public class Rol {

  @Id
  @Column(name = "rol_id")
  private Long rolId;

  @NotBlank
  @Column(name = "nombre_rol", nullable = false, length = 50)
  private String nombreRol; // CLIENTE, ADMIN, SOPORTE
}
