package com.miapeve.finance.controller;

import com.miapeve.finance.dto.SemenceDTO;
import com.miapeve.finance.dto.SemenceRequest;
import com.miapeve.finance.security.AuthInterceptor;
import com.miapeve.finance.service.SemenceService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/semences")
public class SemenceController {

    private final SemenceService semenceService;

    public SemenceController(SemenceService semenceService) {
        this.semenceService = semenceService;
    }

    @GetMapping
    public List<SemenceDTO> findAll() {
        return semenceService.findAll();
    }

    @PostMapping
    public SemenceDTO create(@Valid @RequestBody SemenceRequest request, HttpServletRequest httpRequest) {
        Long utilisateurId = (Long) httpRequest.getAttribute(AuthInterceptor.UTILISATEUR_ID_ATTR);
        return semenceService.create(request, utilisateurId);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        semenceService.delete(id);
    }
}
