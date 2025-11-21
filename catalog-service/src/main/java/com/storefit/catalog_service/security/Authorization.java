package com.storefit.catalog_service.security;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public final class Authorization {

    private Authorization() {}

    public static RequestUser fromHeaders(String rut, String rol) {
        if (rut == null || rut.isBlank() || rol == null || rol.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Faltan headers de autenticación");
        }
        return new RequestUser(rut, rol);
    }

    public static void requireAdmin(RequestUser user) {
        if (!user.isAdmin()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Se requiere rol ADMIN");
        }
    }

    public static void requireSupport(RequestUser user) {
        if (!user.isSoporte()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Se requiere rol SOPORTE");
        }
    }

    public static void requireCliente(RequestUser user) {
        if (!user.isCliente()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Se requiere rol CLIENTE");
        }
    }

    public static void requireOwnerOrSupport(RequestUser user, String rutOwner) {
        if (!(user.isSoporte() || user.getRut().equalsIgnoreCase(rutOwner))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo el dueño o SOPORTE pueden acceder");
        }
    }

    public static void requireOwnerOrAdmin(RequestUser user, String rutOwner) {
        if (!(user.isAdmin() || user.getRut().equalsIgnoreCase(rutOwner))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo el dueño o ADMIN pueden acceder");
        }
    }
}

