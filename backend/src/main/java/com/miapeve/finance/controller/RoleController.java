package com.miapeve.finance.controller;

import com.miapeve.finance.dto.PermissionModuleDTO;
import com.miapeve.finance.dto.RoleDTO;
import com.miapeve.finance.dto.RoleRequest;
import com.miapeve.finance.service.RoleService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    public List<RoleDTO> findAll() {
        return roleService.findAll();
    }

    @GetMapping("/catalog")
    public List<PermissionModuleDTO> catalog() {
        return roleService.getCatalog();
    }

    @PostMapping
    public RoleDTO create(@Valid @RequestBody RoleRequest request) {
        return roleService.create(request);
    }

    @PutMapping("/{id}")
    public RoleDTO update(@PathVariable Long id, @Valid @RequestBody RoleRequest request) {
        return roleService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        roleService.delete(id);
    }
}
