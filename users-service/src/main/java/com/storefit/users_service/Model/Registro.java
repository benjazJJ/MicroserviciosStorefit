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
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Entity
@Table(name = "registro",
  indexes = { @Index(name = "idx_registro_usuario", columnList = "usuario", unique = true) })
@NoArgsConstructor
@AllArgsConstructor
@Data
@Schema(description = "Credenciales de acceso y rol asociado a un usuario")

public class Registro {

  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Schema(description = "ID interno de registro", example = "10")
  private Long id;

  @Column(name = "rol_id", nullable = false)
  @Schema(description = "ID del rol asignado", example = "1")
  private Long rolId;

  @Column(name = "rol_nombre", nullable = true, length = 50)
  @Schema(description = "Nombre del rol", example = "CLIENTE")
  private String rolNombre;

  @NotBlank @Column(name = "usuario", nullable = false, length = 60)
  @Schema(description = "Nombre de usuario (correo)", example = "juan@example.com")
  private String usuario;

  @NotBlank @Column(name = "contrasenia", nullable = false, length = 120)
  @Schema(description = "Contraseña en texto plano para ejemplo (no usar en prod)", example = "ClaveSegura123")
  private String contrasenia;

  @NotBlank @Column(name = "rut", nullable = false, length = 15)
  @Schema(description = "RUT del usuario", example = "12345678-9")
  private String rut;

  @Column(name = "direccion", length = 200)
  @Schema(description = "Dirección registrada", example = "Calle Falsa 123")
  private String address;
}
