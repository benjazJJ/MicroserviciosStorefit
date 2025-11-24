package com.storefit.support_service.security;

// Constantes de roles usados para autorización en soporte
public final class Roles {
    public static final String ADMIN = "ADMIN";    // Administrador (no accede a soporte)
    public static final String CLIENTE = "CLIENTE"; // Cliente
    public static final String SOPORTE = "SOPORTE"; // Soporte

    private Roles() {}
}
