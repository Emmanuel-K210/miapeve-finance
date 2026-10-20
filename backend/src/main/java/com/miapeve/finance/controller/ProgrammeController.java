package com.miapeve.finance.controller;

import com.miapeve.finance.dto.ProgrammeDTO;
import com.miapeve.finance.dto.ProgrammeRequest;
import com.miapeve.finance.security.AuthInterceptor;
import com.miapeve.finance.service.ProgrammeService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/programmes")
public class ProgrammeController {

    private final ProgrammeService programmeService;

    public ProgrammeController(ProgrammeService programmeService) {
        this.programmeService = programmeService;
    }

    @GetMapping
    public List<ProgrammeDTO> findAll() {
        return programmeService.findAll();
    }

    @PostMapping
    public ProgrammeDTO create(@Valid @RequestBody ProgrammeRequest request, HttpServletRequest httpRequest) {
        Long utilisateurId = (Long) httpRequest.getAttribute(AuthInterceptor.UTILISATEUR_ID_ATTR);
        return programmeService.create(request, utilisateurId);
    }

    @PutMapping("/{id}")
    public ProgrammeDTO update(@PathVariable Long id, @Valid @RequestBody ProgrammeRequest request, HttpServletRequest httpRequest) {
        Long utilisateurId = (Long) httpRequest.getAttribute(AuthInterceptor.UTILISATEUR_ID_ATTR);
        return programmeService.update(id, request, utilisateurId);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        programmeService.delete(id);
    }
}
