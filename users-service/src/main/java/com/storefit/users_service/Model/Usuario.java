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

public class Usuario {

  @Id
  @Column(name = "rut", length = 15)
  private String rut;

  @NotBlank @Column(name = "nombre", nullable = false, length = 80)
  private String nombre;

  @NotBlank @Column(name = "apellidos", nullable = false, length = 120)
  private String apellidos;

  @Email @NotBlank
  @Column(name = "correo_electronico", nullable = false, length = 120)
  private String correo;

  @Column(name = "telefono", length = 20)
  private String telefono;

  @Column(name = "direccion", length = 200)
  private String direccion;

  @Column(name = "fecha_nacimiento", length = 10)
  private String fechaNacimiento;  // yyyy-mm-dd

  @Column(name = "foto_uri", length = 250)
  private String fotoUri;
}
