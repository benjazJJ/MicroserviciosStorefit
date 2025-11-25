package com.storefit.users_service.Service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
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
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import com.storefit.users_service.Model.Registro;
import com.storefit.users_service.Model.Rol;
import com.storefit.users_service.Model.Usuario;
import com.storefit.users_service.Repository.RegistroRepository;
import com.storefit.users_service.Repository.RolRepository;
import com.storefit.users_service.Repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class RegistroServiceTest {

    @Mock
    private RegistroRepository registroRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private RolRepository rolRepository;

    @Captor
    private ArgumentCaptor<Registro> registroCaptor;

    private RegistroService service;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @BeforeEach
    void setup() {
        service = new RegistroService(registroRepository, usuarioRepository, rolRepository);
    }

    @Test
    void createSetsDefaultRoleAndEncodesPassword() {
        when(registroRepository.existsByUsuarioIgnoreCase("user@test.com")).thenReturn(false);
        when(rolRepository.findById(1L)).thenReturn(Optional.of(Rol.builder().rolId(1L).nombreRol("CLIENTE").build()));
        when(registroRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Registro input = new Registro(null, null, null, "user@test.com", "plainSecret", "12.345.678-9", "Calle 1");
        Registro saved = service.create(input);

        assertEquals(1L, saved.getRolId());
        assertEquals("CLIENTE", saved.getRolNombre());
        verify(registroRepository).save(registroCaptor.capture());
        assertTrue(encoder.matches("plainSecret", registroCaptor.getValue().getContrasenia()));
    }

    @Test
    void createFailsWhenUsuarioAlreadyExists() {
        when(registroRepository.existsByUsuarioIgnoreCase("dup@test.com")).thenReturn(true);

        Registro input = new Registro(null, null, null, "dup@test.com", "secret", "12.345.678-9", null);
        assertThrows(IllegalArgumentException.class, () -> service.create(input));
        verify(registroRepository, never()).save(any());
    }

    @Test
    void validarLoginOk() {
        Registro reg = new Registro(1L, 1L, "CLIENTE", "user@test.com", encoder.encode("secret"), "12.345.678-9",
                null);
        when(registroRepository.findByUsuarioIgnoreCase("user@test.com")).thenReturn(Optional.of(reg));

        assertTrue(service.validarLogin("user@test.com", "secret"));
    }

    @Test
    void validarLoginBadPasswordThrowsUnauthorized() {
        Registro reg = new Registro(1L, 1L, "CLIENTE", "user@test.com", encoder.encode("secret"), "12.345.678-9",
                null);
        when(registroRepository.findByUsuarioIgnoreCase("user@test.com")).thenReturn(Optional.of(reg));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.validarLogin("user@test.com", "wrong"));
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
    }

    @Test
    void cambiarContraseniaActualIncorrectaLanzaUnauthorized() {
        Registro reg = new Registro(1L, 1L, "CLIENTE", "user@test.com", encoder.encode("secret"), "12.345.678-9",
                null);
        when(registroRepository.findByUsuarioIgnoreCase("user@test.com")).thenReturn(Optional.of(reg));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.cambiarContrasenia("user@test.com", "bad-current", "newpass"));
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
        verify(registroRepository, never()).save(any());
    }

    @Test
    void cambiarContraseniaResuelvePorCorreoYActualizaHash() {
        Usuario usuario = new Usuario("12.345.678-9", "Nombre", "Apellido", "mail@test.com", null, null, null, null);
        Registro reg = new Registro(1L, 1L, "CLIENTE", "mail@test.com", encoder.encode("oldpass"), "12.345.678-9",
                null);

        when(registroRepository.findByUsuarioIgnoreCase("mail@test.com")).thenReturn(Optional.empty());
        when(usuarioRepository.findByCorreoIgnoreCase("mail@test.com")).thenReturn(Optional.of(usuario));
        when(registroRepository.findByRut("12.345.678-9")).thenReturn(Optional.of(reg));

        service.cambiarContrasenia("mail@test.com", "oldpass", "newpass");

        verify(registroRepository).save(argThat(r -> encoder.matches("newpass", r.getContrasenia())));
    }
}
