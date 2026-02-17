package com.casa.security;

import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import com.casa.api.dto.auth.AuthResponse;
import com.casa.service.AuthService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final AuthService authService;

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    public OAuth2AuthenticationSuccessHandler(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public void onAuthenticationSuccess(
        HttpServletRequest request,
        HttpServletResponse response,
        Authentication authentication
    ) throws IOException, ServletException {
        OAuth2User user = (OAuth2User) authentication.getPrincipal();

        String email = user.getAttribute("email");
        if (email == null || email.isBlank()) {
            response.sendRedirect(frontendUrl + "/login?oauthError=missing_email");
            return;
        }

        Object rawName = user.getAttribute("name");
        String displayName = (rawName instanceof String name && !name.isBlank()) ? name : email;

        AuthResponse authResponse = authService.loginWithGoogle(email, displayName);

        String redirect = UriComponentsBuilder
            .fromUriString(frontendUrl + "/auth/callback")
            .queryParam("token", authResponse.accessToken())
            .build(true)
            .toUriString();

        response.sendRedirect(redirect);
    }
}
