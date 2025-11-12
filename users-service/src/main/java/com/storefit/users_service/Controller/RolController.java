package com.storefit.users_service.Controller;

import com.storefit.users_service.Model.Rol;
import com.storefit.users_service.Service.RolService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
public class RolController {

    private final RolService service;

    @GetMapping
    public List<Rol> all() { return service.findAll(); }

    @GetMapping("/{id}")
    public Rol byId(@PathVariable Long id) { return service.findById(id); }
}
