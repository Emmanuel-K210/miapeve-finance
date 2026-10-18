package com.miapeve.finance.controller;

import com.miapeve.finance.dto.CompteDTO;
import com.miapeve.finance.dto.CompteRequest;
import com.miapeve.finance.security.AuthInterceptor;
import com.miapeve.finance.service.CompteService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comptes")
public class CompteController {

    private final CompteService compteService;

    public CompteController(CompteService compteService) {
        this.compteService = compteService;
    }

    @GetMapping
    public List<CompteDTO> findAll() {
        return compteService.findAll();
    }

    @GetMapping("/{id}")
    public CompteDTO findById(@PathVariable Long id) {
        return compteService.findById(id);
    }

    @PostMapping
    public CompteDTO create(@Valid @RequestBody CompteRequest request, HttpServletRequest httpRequest) {
        return compteService.create(request, currentUserId(httpRequest));
    }

    @PutMapping("/{id}")
    public CompteDTO update(@PathVariable Long id, @Valid @RequestBody CompteRequest request, HttpServletRequest httpRequest) {
        return compteService.update(id, request, currentUserId(httpRequest));
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        compteService.delete(id);
    }

    private Long currentUserId(HttpServletRequest request) {
        return (Long) request.getAttribute(AuthInterceptor.UTILISATEUR_ID_ATTR);
    }
}
