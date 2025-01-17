package com.hl.fambud.configuration;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.server.WebFilter;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;

public class SecurityTestConfig {

    @Bean
    public WebFilter securityTestWebFilter() {
        return (exchange, chain) -> {
            // Create a mock JWT with necessary claims and headers
            Jwt jwt = Jwt.withTokenValue("test-token")
                .claim("sub", "test-user") // Subject claim
                .claim("scope", "read write") // Scopes
                .header("alg", "none") // Algorithm
                .build();

            // Wrap the JWT in an authentication token
            JwtAuthenticationToken authenticationToken = new JwtAuthenticationToken(
                jwt, List.of(
                new SimpleGrantedAuthority("SCOPE_read"),
                new SimpleGrantedAuthority("SCOPE_write")
            )
            );

            // Attach the authentication token to the security context
            return chain.filter(exchange)
                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authenticationToken));
        };
    }


    @Bean
    public ReactiveJwtDecoder reactiveJwtDecoder() {
        ReactiveJwtDecoder jwtDecoder = Mockito.mock(ReactiveJwtDecoder.class);
        Mockito.when(jwtDecoder.decode("test-token")).thenReturn(Mono.just(mockJwt()));
        return jwtDecoder;
    }

    @Bean
    public Jwt mockJwt() {
        return Jwt.withTokenValue("test-token")
            .header("alg", "none")
            .claim("sub", "test-user")
            .claim("scope", "read write")
            .claim("aud", "test-audience")
            .claim("iss", "https://mock-issuer-for-tests.com/")
            .claim("iat", Instant.now())
            .claim("exp", Instant.now().plusSeconds(3600))
            .claims(claims -> claims.put("custom-claim", "custom-value"))
            .build();
    }
}
