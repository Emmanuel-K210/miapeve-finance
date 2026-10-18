package com.miapeve.finance.security;

import com.miapeve.finance.entity.Utilisateur;
import com.miapeve.finance.permission.PermissionCatalog;
import com.miapeve.finance.repository.UtilisateurRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;
import java.util.Set;

public class AuthInterceptor implements HandlerInterceptor {

    public static final String UTILISATEUR_ID_ATTR = "utilisateurId";
    public static final String PERMISSIONS_ATTR = "permissions";

    /**
     * Fait correspondre le premier segment de chemin d'une route /api/...
     * au module du catalogue de permissions. Les routes absentes de cette
     * table (auth, dashboard, health) ne sont soumises à aucun contrôle de
     * permission au-delà d'être authentifié.
     */
    private static final Map<String, String> PATH_TO_MODULE = Map.of(
            "comptes", "COMPTES",
            "transactions", "TRANSACTIONS",
            "semences", "SEMENCES",
            "depenses", "DEPENSES",
            "programmes", "PROGRAMMES",
            "budgets", "BUDGETS",
            "utilisateurs", "UTILISATEURS",
            "roles", "ROLES"
    );

    private final SessionService sessionService;
    private final UtilisateurRepository utilisateurRepository;

    public AuthInterceptor(SessionService sessionService, UtilisateurRepository utilisateurRepository) {
        this.sessionService = sessionService;
        this.utilisateurRepository = utilisateurRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String authHeader = request.getHeader("Authorization");
        String token = (authHeader != null && authHeader.startsWith("Bearer "))
                ? authHeader.substring("Bearer ".length())
                : null;

        Long utilisateurId = sessionService.resolve(token);
        if (utilisateurId == null) {
            return unauthorized(response, "Session invalide ou expirée");
        }

        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId).orElse(null);
        if (utilisateur == null || !utilisateur.isActif()) {
            return unauthorized(response, "Session invalide ou expirée");
        }

        Set<String> permissions = utilisateur.getRole() != null ? utilisateur.getRole().getPermissions() : Set.of();

        String module = resolveModule(request.getRequestURI());
        if (module != null) {
            boolean isMutation = !"GET".equalsIgnoreCase(request.getMethod());
            String required = isMutation ? PermissionCatalog.manage(module) : PermissionCatalog.view(module);
            if (!permissions.contains(required)) {
                return forbidden(response, "Vous n'avez pas la permission nécessaire pour cette action");
            }
        }

        request.setAttribute(UTILISATEUR_ID_ATTR, utilisateurId);
        request.setAttribute(PERMISSIONS_ATTR, permissions);
        return true;
    }

    private String resolveModule(String uri) {
        String[] segments = uri.split("/");
        // uri = /api/<segment>/...  -> segments = ["", "api", "<segment>", ...]
        if (segments.length < 3) {
            return null;
        }
        return PATH_TO_MODULE.get(segments[2]);
    }

    private boolean unauthorized(HttpServletResponse response, String message) throws java.io.IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        writeJsonError(response, message);
        return false;
    }

    private boolean forbidden(HttpServletResponse response, String message) throws java.io.IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        writeJsonError(response, message);
        return false;
    }

    private void writeJsonError(HttpServletResponse response, String message) throws java.io.IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"message\":\"" + message + "\"}");
    }
}
