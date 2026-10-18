package com.miapeve.finance.service;

import com.miapeve.finance.dto.ChangePasswordRequest;
import com.miapeve.finance.dto.LoginRequest;
import com.miapeve.finance.dto.UtilisateurDTO;
import com.miapeve.finance.entity.Utilisateur;
import com.miapeve.finance.repository.UtilisateurRepository;
import com.miapeve.finance.security.PasswordHasher;
import com.miapeve.finance.security.SessionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UtilisateurRepository utilisateurRepository;
    private final SessionService sessionService;

    public AuthService(UtilisateurRepository utilisateurRepository, SessionService sessionService) {
        this.utilisateurRepository = utilisateurRepository;
        this.sessionService = sessionService;
    }

    public record LoginResult(String token, UtilisateurDTO utilisateur) {
    }

    @Transactional(readOnly = true)
    public LoginResult login(LoginRequest request) {
        Utilisateur utilisateur = utilisateurRepository.findByNomUtilisateur(request.nomUtilisateur())
                .orElseThrow(() -> new IllegalArgumentException("Nom d'utilisateur ou mot de passe incorrect"));

        if (!PasswordHasher.verifyPassword(request.motDePasse(), utilisateur.getMotDePasse())) {
            throw new IllegalArgumentException("Nom d'utilisateur ou mot de passe incorrect");
        }

        if (!utilisateur.isActif()) {
            throw new IllegalArgumentException("Ce compte utilisateur est désactivé");
        }

        String token = sessionService.createSession(utilisateur);
        return new LoginResult(token, toDTO(utilisateur));
    }

    public void logout(String token) {
        sessionService.invalidate(token);
    }

    @Transactional(readOnly = true)
    public UtilisateurDTO getUtilisateur(Long id) {
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));
        return toDTO(utilisateur);
    }

    @Transactional
    public void changeOwnPassword(Long utilisateurId, ChangePasswordRequest request) {
        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));

        if (!PasswordHasher.verifyPassword(request.motDePasseActuel(), utilisateur.getMotDePasse())) {
            throw new IllegalArgumentException("Mot de passe actuel incorrect");
        }

        utilisateur.setMotDePasse(PasswordHasher.hashPassword(request.nouveauMotDePasse()));
        utilisateurRepository.save(utilisateur);
    }

    private UtilisateurDTO toDTO(Utilisateur utilisateur) {
        return UtilisateurMapper.toDTO(utilisateur);
    }
}
