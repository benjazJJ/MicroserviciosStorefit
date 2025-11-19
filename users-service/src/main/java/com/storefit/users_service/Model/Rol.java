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
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Entity
@Table(name = "rol", uniqueConstraints = @UniqueConstraint(columnNames = "nombre_rol"))
@AllArgsConstructor
@NoArgsConstructor
@Data
@Schema(description = "Rol del sistema (CLIENTE, ADMIN, SOPORTE)")
@Builder


public class Rol {

  @Id
  @Column(name = "rol_id")
  @Schema(description = "ID del rol", example = "1")
  private Long rolId;

  @NotBlank
  @Column(name = "nombre_rol", nullable = false, length = 50)
  @Schema(description = "Nombre del rol", example = "CLIENTE")
  private String nombreRol; // CLIENTE, ADMIN, SOPORTE
}
