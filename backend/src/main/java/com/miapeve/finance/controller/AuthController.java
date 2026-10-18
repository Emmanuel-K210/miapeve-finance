package com.miapeve.finance.controller;

import com.miapeve.finance.dto.LoginRequest;
import com.miapeve.finance.dto.UtilisateurDTO;
import com.miapeve.finance.security.AuthInterceptor;
import com.miapeve.finance.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public AuthService.LoginResult login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/logout")
    public void logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            authService.logout(authHeader.substring("Bearer ".length()));
        }
    }

    @GetMapping("/me")
    public UtilisateurDTO me(HttpServletRequest request) {
        Long utilisateurId = (Long) request.getAttribute(AuthInterceptor.UTILISATEUR_ID_ATTR);
        return authService.getUtilisateur(utilisateurId);
    }
}
