package com.storefit.users_service.Repository;


import com.storefit.users_service.Model.Rol;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RolRepository extends JpaRepository<Rol, Long> {
    Optional<Rol> findByNombreRolIgnoreCase(String nombreRol);
}
