package com.storefit.users_service.Model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Entity
@Table(name = "usuarios",
  indexes = {
    @Index(name = "idx_usuario_correo", columnList = "correo_electronico", unique = true),
    @Index(name = "idx_usuario_telefono", columnList = "telefono", unique = true)
})
@NoArgsConstructor 
@AllArgsConstructor
@Data
@Schema(description = "Perfil de usuario identificado por RUT")

public class Usuario {

  @Id
  @Column(name = "rut", length = 15)
  @Schema(description = "RUT del usuario", example = "12345678-9")
  private String rut;

  @NotBlank @Column(name = "nombre", nullable = false, length = 80)
  @Schema(description = "Nombre", example = "Juan")
  private String nombre;

  @NotBlank @Column(name = "apellidos", nullable = false, length = 120)
  @Schema(description = "Apellidos", example = "Pérez López")
  private String apellidos;

  @Email @NotBlank
  @Column(name = "correo_electronico", nullable = false, length = 120)
  @Schema(description = "Correo electrónico", example = "juan@example.com")
  private String correo;

  @Column(name = "telefono", length = 20)
  @Schema(description = "Teléfono", example = "987654321")
  private String telefono;

  @Column(name = "direccion", length = 200)
  @Schema(description = "Dirección", example = "Calle Falsa 123")
  private String direccion;

  @Column(name = "fecha_nacimiento", length = 10)
  @Schema(description = "Fecha de nacimiento (yyyy-mm-dd)", example = "1995-10-10")
  private String fechaNacimiento;  // yyyy-mm-dd

  @Column(name = "foto_uri", length = 250)
  @Schema(description = "URI de foto de perfil", example = "/img/profile/u1.png")
  private String fotoUri;
}
