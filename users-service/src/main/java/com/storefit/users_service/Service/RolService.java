package com.storefit.users_service.Service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.storefit.users_service.Model.Rol;
import com.storefit.users_service.Repository.RolRepository;

import lombok.RequiredArgsConstructor;
import jakarta.persistence.EntityNotFoundException;

@Service
@RequiredArgsConstructor
public class RolService {
    private final RolRepository repo;

    public List<Rol> findAll() { return repo.findAll(); }

    public Rol findById(Long id) {
        return repo.findById(id).orElseThrow(() -> new EntityNotFoundException("Rol no encontrado: " + id));
    }

}

