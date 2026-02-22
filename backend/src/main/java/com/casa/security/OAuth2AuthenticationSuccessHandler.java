package com.casa.security;

import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import com.casa.api.dto.auth.AuthResponse;
import com.casa.service.GoogleCalendarService;
import com.casa.service.AuthService;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final AuthService authService;
    private final OAuth2AuthorizedClientService authorizedClientService;
    private final GoogleCalendarService googleCalendarService;

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    public OAuth2AuthenticationSuccessHandler(
        AuthService authService,
        OAuth2AuthorizedClientService authorizedClientService,
        GoogleCalendarService googleCalendarService
    ) {
        this.authService = authService;
        this.authorizedClientService = authorizedClientService;
        this.googleCalendarService = googleCalendarService;
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

        if (authentication instanceof OAuth2AuthenticationToken oauthToken) {
            OAuth2AuthorizedClient authorizedClient = authorizedClientService.loadAuthorizedClient(
                oauthToken.getAuthorizedClientRegistrationId(),
                oauthToken.getName()
            );
            if (authorizedClient != null) {
                OAuth2AccessToken accessToken = authorizedClient.getAccessToken();
                OAuth2RefreshToken refreshToken = authorizedClient.getRefreshToken();
                googleCalendarService.storeTokens(
                    authResponse.user().id(),
                    accessToken == null ? null : accessToken.getTokenValue(),
                    accessToken == null || accessToken.getExpiresAt() == null
                        ? null
                        : OffsetDateTime.ofInstant(accessToken.getExpiresAt(), ZoneOffset.UTC),
                    refreshToken == null ? null : refreshToken.getTokenValue()
                );
            }
        }

        String redirect = UriComponentsBuilder
            .fromUriString(frontendUrl + "/auth/callback")
            .queryParam("token", authResponse.accessToken())
            .build(true)
            .toUriString();

        response.sendRedirect(redirect);
    }
}
