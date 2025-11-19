package com.storefit.orders_service.Model;

import lombok.Data;

@Data
public class UsuarioDTO {
    private String rut;
    private String nombre;
    private String apellidos;
    private String correo;
    private String telefono;
    private String direccion;
    private String fechaNacimiento;
    private String fotoUri;
}
