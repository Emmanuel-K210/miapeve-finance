package com.miapeve.finance.security;

import com.miapeve.finance.entity.Utilisateur;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Sessions en mémoire : l'API est servie en localhost pour une seule
 * application desktop à la fois, un simple token opaque suffit (pas besoin
 * de JWT ni de store externe).
 */
@Service
public class SessionService {

    private final Map<String, Long> tokensToUtilisateurId = new ConcurrentHashMap<>();

    public String createSession(Utilisateur utilisateur) {
        String token = UUID.randomUUID().toString();
        tokensToUtilisateurId.put(token, utilisateur.getId());
        return token;
    }

    public Long resolve(String token) {
        if (token == null) {
            return null;
        }
        return tokensToUtilisateurId.get(token);
    }

    public void invalidate(String token) {
        tokensToUtilisateurId.remove(token);
    }
}
