package com.storefit.users_service.security;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

// Utilidades para construir RequestUser y validar rol en users-service
public final class Authorization {

    private Authorization() {}

    // Crea RequestUser desde headers; 401 si faltan
    public static RequestUser fromHeaders(String rut, String rol) {
        if (rut == null || rut.isBlank() || rol == null || rol.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Faltan headers de autenticación");
        }
        // Enforce dotted RUT format for this service
        if (!RutUtils.isDottedFormat(rut)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "RUT debe venir con puntos y guion (ej: 12.345.678-9)");
        }
        return new RequestUser(rut, rol);
    }

    // Requiere ADMIN
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
