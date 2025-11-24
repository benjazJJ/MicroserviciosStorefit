package com.storefit.catalog_service.security;

// Constantes de roles usados para autorización basada en headers
public final class Roles {
    public static final String ADMIN = "ADMIN";    // Rol de administrador
    public static final String CLIENTE = "CLIENTE"; // Rol de cliente
    public static final String SOPORTE = "SOPORTE"; // Rol de soporte

    private Roles() {}
}
