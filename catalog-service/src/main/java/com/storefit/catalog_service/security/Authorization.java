package com.storefit.catalog_service.security;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

// Utilidades para construir el usuario desde headers y validar roles
public final class Authorization {

    private Authorization() {}

    // Crea RequestUser a partir de headers; 401 si faltan
    public static RequestUser fromHeaders(String rut, String rol) {
        if (rut == null || rut.isBlank() || rol == null || rol.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Faltan headers de autenticación");
        }
        return new RequestUser(rut, rol);
    }

    // Requiere rol ADMIN; 403 si no cumple
    public static void requireAdmin(RequestUser user) {
        if (!user.isAdmin()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Se requiere rol ADMIN");
        }
    }

    // Requiere rol SOPORTE; 403 si no cumple
    public static void requireSupport(RequestUser user) {
        if (!user.isSoporte()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Se requiere rol SOPORTE");
        }
    }

    // Requiere rol CLIENTE; 403 si no cumple
    public static void requireCliente(RequestUser user) {
        if (!user.isCliente()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Se requiere rol CLIENTE");
        }
    }

    // Permite al dueño (RUT) o SOPORTE
    public static void requireOwnerOrSupport(RequestUser user, String rutOwner) {
        if (!(user.isSoporte() || user.getRut().equalsIgnoreCase(rutOwner))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo el dueño o SOPORTE pueden acceder");
        }
    }

    // Permite al dueño (RUT) o ADMIN
    public static void requireOwnerOrAdmin(RequestUser user, String rutOwner) {
        if (!(user.isAdmin() || user.getRut().equalsIgnoreCase(rutOwner))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo el dueño o ADMIN pueden acceder");
        }
    }
}
