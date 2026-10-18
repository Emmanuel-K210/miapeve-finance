package com.miapeve.finance.service;

import com.miapeve.finance.dto.PasswordResetRequest;
import com.miapeve.finance.dto.UtilisateurDTO;
import com.miapeve.finance.dto.UtilisateurRequest;
import com.miapeve.finance.entity.Role;
import com.miapeve.finance.entity.Utilisateur;
import com.miapeve.finance.repository.RoleRepository;
import com.miapeve.finance.repository.UtilisateurRepository;
import com.miapeve.finance.security.PasswordHasher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;
    private final RoleRepository roleRepository;

    public UtilisateurService(UtilisateurRepository utilisateurRepository, RoleRepository roleRepository) {
        this.utilisateurRepository = utilisateurRepository;
        this.roleRepository = roleRepository;
    }

    @Transactional(readOnly = true)
    public List<UtilisateurDTO> findAll() {
        return utilisateurRepository.findAll().stream().map(UtilisateurMapper::toDTO).toList();
    }

    @Transactional
    public UtilisateurDTO create(UtilisateurRequest request) {
        if (request.motDePasse() == null || request.motDePasse().isBlank()) {
            throw new IllegalArgumentException("Le mot de passe est obligatoire à la création");
        }
        if (utilisateurRepository.findByNomUtilisateur(request.nomUtilisateur()).isPresent()) {
            throw new IllegalArgumentException("Ce nom d'utilisateur est déjà pris");
        }

        Role role = roleRepository.findById(request.roleId())
                .orElseThrow(() -> new IllegalArgumentException("Rôle introuvable: " + request.roleId()));

        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setNom(request.nom());
        utilisateur.setPrenom(request.prenom());
        utilisateur.setNomUtilisateur(request.nomUtilisateur());
        utilisateur.setMotDePasse(PasswordHasher.hashPassword(request.motDePasse()));
        utilisateur.setRole(role);
        utilisateur.setActif(request.actif());

        return UtilisateurMapper.toDTO(utilisateurRepository.save(utilisateur));
    }

    @Transactional
    public UtilisateurDTO update(Long id, UtilisateurRequest request, Long actorId) {
        Utilisateur utilisateur = getOrThrow(id);

        Role role = roleRepository.findById(request.roleId())
                .orElseThrow(() -> new IllegalArgumentException("Rôle introuvable: " + request.roleId()));

        if (id.equals(actorId) && !request.actif()) {
            throw new IllegalArgumentException("Vous ne pouvez pas désactiver votre propre compte");
        }

        utilisateur.setNom(request.nom());
        utilisateur.setPrenom(request.prenom());
        utilisateur.setRole(role);
        utilisateur.setActif(request.actif());

        if (request.motDePasse() != null && !request.motDePasse().isBlank()) {
            utilisateur.setMotDePasse(PasswordHasher.hashPassword(request.motDePasse()));
        }

        return UtilisateurMapper.toDTO(utilisateurRepository.save(utilisateur));
    }

    @Transactional
    public void resetPassword(Long id, PasswordResetRequest request) {
        Utilisateur utilisateur = getOrThrow(id);
        utilisateur.setMotDePasse(PasswordHasher.hashPassword(request.nouveauMotDePasse()));
        utilisateurRepository.save(utilisateur);
    }

    @Transactional
    public void delete(Long id, Long actorId) {
        if (id.equals(actorId)) {
            throw new IllegalArgumentException("Vous ne pouvez pas supprimer votre propre compte");
        }
        utilisateurRepository.deleteById(id);
    }

    private Utilisateur getOrThrow(Long id) {
        return utilisateurRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable: " + id));
    }
}
