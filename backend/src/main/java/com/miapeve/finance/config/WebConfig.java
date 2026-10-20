package com.miapeve.finance.config;

import com.miapeve.finance.repository.AuditLogRepository;
import com.miapeve.finance.repository.UtilisateurRepository;
import com.miapeve.finance.security.AuthInterceptor;
import com.miapeve.finance.security.SessionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final SessionService sessionService;
    private final UtilisateurRepository utilisateurRepository;
    private final AuditLogRepository auditLogRepository;

    @Value("${miapeve.cors.allowed-origins}")
    private String allowedOrigins;

    public WebConfig(SessionService sessionService, UtilisateurRepository utilisateurRepository, AuditLogRepository auditLogRepository) {
        this.sessionService = sessionService;
        this.utilisateurRepository = utilisateurRepository;
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(allowedOrigins.split(","))
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AuthInterceptor(sessionService, utilisateurRepository, auditLogRepository))
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/login", "/api/health");
    }
}
