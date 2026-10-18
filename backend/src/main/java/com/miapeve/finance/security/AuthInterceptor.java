package com.miapeve.finance.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

public class AuthInterceptor implements HandlerInterceptor {

    public static final String UTILISATEUR_ID_ATTR = "utilisateurId";

    private final SessionService sessionService;

    public AuthInterceptor(SessionService sessionService) {
        this.sessionService = sessionService;
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
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"message\":\"Session invalide ou expirée\"}");
            return false;
        }

        request.setAttribute(UTILISATEUR_ID_ATTR, utilisateurId);
        return true;
    }
}
