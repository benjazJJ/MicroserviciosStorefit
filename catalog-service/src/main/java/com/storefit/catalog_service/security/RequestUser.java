package com.storefit.catalog_service.security;

// Representa al usuario autenticado extraído de los headers
public class RequestUser {
    private final String rut; // RUT del usuario desde X-User-Rut
    private final String rol; // Rol del usuario desde X-User-Rol

    public RequestUser(String rut, String rol) {
        this.rut = rut;
        this.rol = rol;
    }

    public String getRut() { return rut; }
    public String getRol() { return rol; }

    // Helpers de rol para validar permisos
    public boolean isAdmin()   { return Roles.ADMIN.equalsIgnoreCase(rol); }
    public boolean isCliente() { return Roles.CLIENTE.equalsIgnoreCase(rol); }
    public boolean isSoporte() { return Roles.SOPORTE.equalsIgnoreCase(rol); }
}
