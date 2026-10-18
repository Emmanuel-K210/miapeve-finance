package com.miapeve.finance.controller;

import com.miapeve.finance.dto.DepenseDTO;
import com.miapeve.finance.dto.DepenseRequest;
import com.miapeve.finance.security.AuthInterceptor;
import com.miapeve.finance.service.DepenseService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/depenses")
public class DepenseController {

    private final DepenseService depenseService;

    public DepenseController(DepenseService depenseService) {
        this.depenseService = depenseService;
    }

    @GetMapping
    public List<DepenseDTO> findAll(@RequestParam(required = false) Long compteId) {
        return compteId != null ? depenseService.findByCompte(compteId) : depenseService.findAll();
    }

    @PostMapping
    public DepenseDTO create(@Valid @RequestBody DepenseRequest request, HttpServletRequest httpRequest) {
        Long utilisateurId = (Long) httpRequest.getAttribute(AuthInterceptor.UTILISATEUR_ID_ATTR);
        return depenseService.create(request, utilisateurId);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        depenseService.delete(id);
    }
}
