package com.miapeve.finance.config;

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

    @Value("${miapeve.cors.allowed-origins}")
    private String allowedOrigins;

    public WebConfig(SessionService sessionService, UtilisateurRepository utilisateurRepository) {
        this.sessionService = sessionService;
        this.utilisateurRepository = utilisateurRepository;
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
        registry.addInterceptor(new AuthInterceptor(sessionService, utilisateurRepository))
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/login", "/api/health");
    }
}
