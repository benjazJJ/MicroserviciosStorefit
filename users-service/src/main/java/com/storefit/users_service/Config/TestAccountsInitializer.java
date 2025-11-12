package com.storefit.users_service.Config;

import com.storefit.users_service.Model.Registro;
import com.storefit.users_service.Model.Usuario;
import com.storefit.users_service.Repository.RegistroRepository;
import com.storefit.users_service.Repository.UsuarioRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class TestAccountsInitializer {

    private final RegistroRepository registroRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder encoder = new BCryptPasswordEncoder();

    @PostConstruct
    @Transactional
    public void init() {
        crearCuentaSiNoExiste(
                // usuario
                "11111111-1", "Cliente", "Demo", "cliente@test.com", "111111111", "Calle 1", "1990-01-01",
                "cliente", "Cliente123!", 1L
        );

        crearCuentaSiNoExiste(
                // admin
                "22222222-2", "Admin", "Demo", "admin@test.com", "222222222", "Calle 2", "1985-05-05",
                "admin", "Admin123!", 2L
        );

        crearCuentaSiNoExiste(
                // soporte
                "33333333-3", "Soporte", "Demo", "soporte@test.com", "333333333", "Calle 3", "1988-08-08",
                "soporte", "Soporte123!", 3L
        );
    }

    private void crearCuentaSiNoExiste(
            String rut, String nombre, String apellidos, String correo, String telefono,
            String direccion, String fechaNacimiento, String usuario, String contrasenia, Long rolId
    ) {
        if (!registroRepository.existsByUsuarioIgnoreCase(usuario)) {
            if (!usuarioRepository.existsById(rut)) {
                Usuario u = new Usuario();
                u.setRut(rut);
                u.setNombre(nombre);
                u.setApellidos(apellidos);
                u.setCorreo(correo);
                u.setTelefono(telefono);
                u.setDireccion(direccion);
                u.setFechaNacimiento(fechaNacimiento);
                usuarioRepository.save(u);
            }

            Registro r = new Registro();
            r.setRut(rut);
            r.setUsuario(usuario);
            r.setContrasenia(encoder.encode(contrasenia));
            r.setAddress(direccion);
            r.setRolId(rolId);
            if (rolId == 1L) r.setRolNombre("CLIENTE");
            else if (rolId == 2L) r.setRolNombre("ADMIN");
            else if (rolId == 3L) r.setRolNombre("SOPORTE");
            registroRepository.save(r);
        }
    }
}
