package com.miapeve.finance.controller;

import com.miapeve.finance.dto.PasswordResetRequest;
import com.miapeve.finance.dto.UtilisateurDTO;
import com.miapeve.finance.dto.UtilisateurRequest;
import com.miapeve.finance.security.AuthInterceptor;
import com.miapeve.finance.service.UtilisateurService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/utilisateurs")
public class UtilisateurController {

    private final UtilisateurService utilisateurService;

    public UtilisateurController(UtilisateurService utilisateurService) {
        this.utilisateurService = utilisateurService;
    }

    @GetMapping
    public List<UtilisateurDTO> findAll() {
        return utilisateurService.findAll();
    }

    @PostMapping
    public UtilisateurDTO create(@Valid @RequestBody UtilisateurRequest request) {
        return utilisateurService.create(request);
    }

    @PutMapping("/{id}")
    public UtilisateurDTO update(@PathVariable Long id, @Valid @RequestBody UtilisateurRequest request, HttpServletRequest httpRequest) {
        return utilisateurService.update(id, request, currentUserId(httpRequest));
    }

    @PutMapping("/{id}/mot-de-passe")
    public void resetPassword(@PathVariable Long id, @Valid @RequestBody PasswordResetRequest request) {
        utilisateurService.resetPassword(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id, HttpServletRequest httpRequest) {
        utilisateurService.delete(id, currentUserId(httpRequest));
    }

    private Long currentUserId(HttpServletRequest request) {
        return (Long) request.getAttribute(AuthInterceptor.UTILISATEUR_ID_ATTR);
    }
}
