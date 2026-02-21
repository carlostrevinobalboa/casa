package com.casa.security;

import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.JwtException;

@Component
public class WebSocketAuthChannelInterceptor implements ChannelInterceptor {

    private final JwtTokenService jwtTokenService;

    public WebSocketAuthChannelInterceptor(JwtTokenService jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
    }

    @Override
    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null || accessor.getCommand() != StompCommand.CONNECT) {
            return message;
        }

        String authHeader = firstNonBlank(
            accessor.getFirstNativeHeader("Authorization"),
            accessor.getFirstNativeHeader("authorization"),
            accessor.getFirstNativeHeader("token")
        );

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new MessageDeliveryException(message, "Token JWT requerido para WebSocket");
        }

        String token = authHeader.substring(7).trim();
        if (token.isEmpty()) {
            throw new MessageDeliveryException(message, "Token JWT invalido");
        }

        try {
            AppUserPrincipal principal = jwtTokenService.parsePrincipal(token);
            accessor.setUser(new StompPrincipal(principal.userId().toString()));
        } catch (JwtException | IllegalArgumentException ex) {
            throw new MessageDeliveryException(message, ex);
        }

        return message;
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }
}
