package com.storefit.users_service.Service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import com.storefit.users_service.Model.Registro;
import com.storefit.users_service.Model.Rol;
import com.storefit.users_service.Model.Usuario;
import com.storefit.users_service.Repository.RegistroRepository;
import com.storefit.users_service.Repository.RolRepository;
import com.storefit.users_service.Repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private RegistroRepository registroRepository;

    @Mock
    private RolRepository rolRepository;

    private UsuarioService service;

    @Captor
    private ArgumentCaptor<Registro> registroCaptor;

    @BeforeEach
    void setup() {
        service = new UsuarioService(usuarioRepository, registroRepository, rolRepository);
    }

    @Test
    void findByRutFallsBackToUndotted() {
        Usuario undotted = new Usuario("12345678-9", "Nombre", "Apellido", "mail@test.com", null, null, null, null);
        when(usuarioRepository.findById("12.345.678-9")).thenReturn(Optional.empty());
        when(usuarioRepository.findById("12345678-9")).thenReturn(Optional.of(undotted));

        Usuario found = service.findByRut("12.345.678-9");

        assertEquals("12345678-9", found.getRut());
    }

    @Test
    void createRequiresDottedRut() {
        Usuario u = new Usuario("12345678-9", "Nombre", "Apellido", "mail@test.com", null, null, null, null);
        assertThrows(ResponseStatusException.class, () -> service.create(u));
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void createPersistsCanonicalDottedRut() {
        Usuario u = new Usuario("12.345.678-9", "Nombre", "Apellido", "mail@test.com", "123", null, null, null);
        when(usuarioRepository.existsById("12.345.678-9")).thenReturn(false);
        when(usuarioRepository.existsByCorreoIgnoreCase("mail@test.com")).thenReturn(false);
        when(usuarioRepository.existsByTelefono("123")).thenReturn(false);
        when(usuarioRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Usuario saved = service.create(u);

        assertEquals("12.345.678-9", saved.getRut());
    }

    @Test
    void updateRolPropagatesNombreDeRol() {
        Registro reg = new Registro(1L, 1L, "CLIENTE", "u@test.com", "pwd", "12.345.678-9", null);
        Rol admin = Rol.builder().rolId(2L).nombreRol("ADMIN").build();

        when(registroRepository.findByRut("12.345.678-9")).thenReturn(Optional.of(reg));
        when(rolRepository.findById(2L)).thenReturn(Optional.of(admin));

        service.updateRol("12.345.678-9", 2L);

        verify(registroRepository).save(registroCaptor.capture());
        assertEquals(2L, registroCaptor.getValue().getRolId());
        assertEquals("ADMIN", registroCaptor.getValue().getRolNombre());
    }
}
