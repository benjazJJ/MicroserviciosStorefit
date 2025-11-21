package com.storefit.orders_service.security;

public class RequestUser {
    private final String rut;
    private final String rol;

    public RequestUser(String rut, String rol) {
        this.rut = rut;
        this.rol = rol;
    }

    public String getRut() { return rut; }
    public String getRol() { return rol; }

    public boolean isAdmin()   { return Roles.ADMIN.equalsIgnoreCase(rol); }
    public boolean isCliente() { return Roles.CLIENTE.equalsIgnoreCase(rol); }
    public boolean isSoporte() { return Roles.SOPORTE.equalsIgnoreCase(rol); }
}

