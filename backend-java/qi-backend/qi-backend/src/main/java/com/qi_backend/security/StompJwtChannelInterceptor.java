package com.qi_backend.security;

import

        com.qi_backend.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StompJwtChannelInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        StompCommand command = accessor.getCommand();

        if (command == StompCommand.CONNECT) {
            accessor.setUser(authenticate(accessor.getFirstNativeHeader("Authorization")));
        } else if (command == StompCommand.SEND) {
            requireAuthenticated(accessor.getUser());
            if (!"/app/chat.send".equals(accessor.getDestination())) {
                throw new MessagingException("Unsupported STOMP destination");
            }
        } else if (command == StompCommand.SUBSCRIBE) {
            requireAuthenticated(accessor.getUser());
            if (!"/user/queue/messages".equals(accessor.getDestination())) {
                throw new MessagingException("Unsupported STOMP subscription");
            }
        }

        return message;
    }

    private Authentication authenticate(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new MessagingException("A bearer token is required to connect");
        }

        try {
            String token = authorizationHeader.substring(7);
            String email = jwtService.extractUsername(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);
            if (!jwtService.isTokenValid(token, userDetails)) {
                throw new MessagingException("Invalid bearer token");
            }

            return new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
            );
        } catch (MessagingException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw new MessagingException("Invalid or expired bearer token", exception);
        }
    }

    private void requireAuthenticated(java.security.Principal principal) {
        if (principal == null) {
            throw new MessagingException("An authenticated STOMP connection is required");
        }
    }
}
