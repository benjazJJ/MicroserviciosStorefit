package com.storefit.users_service.Repository;

import com.storefit.users_service.Model.Registro;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RegistroRepository extends JpaRepository<Registro, Long> {
    boolean existsByUsuarioIgnoreCase(String usuario);
    Optional<Registro> findByUsuarioIgnoreCase(String usuario);
    Optional<Registro> findByRut(String rut);
}
