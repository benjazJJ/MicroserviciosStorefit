package com.storefit.support_service.security;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

// Utilidades para validar headers y roles en soporte
public final class Authorization {

    private Authorization() {}

    // Construye RequestUser; 401 si faltan headers
    public static RequestUser fromHeaders(String rut, String rol) {
        if (rut == null || rut.isBlank() || rol == null || rol.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Faltan headers de autenticación");
        }
        return new RequestUser(rut, rol);
    }

    // Requiere ADMIN (no usado para soporte funcional)
    public static void requireAdmin(RequestUser user) {
        if (!user.isAdmin()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Se requiere rol ADMIN");
        }
    }

    // Requiere SOPORTE
    public static void requireSupport(RequestUser user) {
        if (!user.isSoporte()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Se requiere rol SOPORTE");
        }
    }

    // Requiere CLIENTE
    public static void requireCliente(RequestUser user) {
        if (!user.isCliente()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Se requiere rol CLIENTE");
        }
    }

    // Dueño o SOPORTE
    public static void requireOwnerOrSupport(RequestUser user, String rutOwner) {
        if (!(user.isSoporte() || user.getRut().equalsIgnoreCase(rutOwner))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo el dueño o SOPORTE pueden acceder");
        }
    }

    // Dueño o ADMIN
    public static void requireOwnerOrAdmin(RequestUser user, String rutOwner) {
        if (!(user.isAdmin() || user.getRut().equalsIgnoreCase(rutOwner))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo el dueño o ADMIN pueden acceder");
        }
    }
}
