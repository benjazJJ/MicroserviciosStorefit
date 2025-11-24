package com.storefit.support_service.security;

// Usuario autenticado desde headers (RUT/ROL)
public class RequestUser {
    private final String rut; // X-User-Rut
    private final String rol; // X-User-Rol

    public RequestUser(String rut, String rol) {
        this.rut = rut;
        this.rol = rol;
    }

    public String getRut() { return rut; }
    public String getRol() { return rol; }

    // Helpers de rol
    public boolean isAdmin()   { return Roles.ADMIN.equalsIgnoreCase(rol); }
    public boolean isCliente() { return Roles.CLIENTE.equalsIgnoreCase(rol); }
    public boolean isSoporte() { return Roles.SOPORTE.equalsIgnoreCase(rol); }
}
