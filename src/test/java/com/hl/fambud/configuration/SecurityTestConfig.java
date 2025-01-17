package com.hl.fambud.configuration;

import jakarta.annotation.PostConstruct;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import reactor.core.publisher.Mono;

import java.time.Instant;

public class SecurityTestConfig {

    @Bean
    public ReactiveJwtDecoder reactiveJwtDecoder() {
        return Mockito.mock(ReactiveJwtDecoder.class);
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

    @PostConstruct
    public void mockJwtDecoderBehavior() {
        ReactiveJwtDecoder jwtDecoder = reactiveJwtDecoder();
        Jwt jwt = mockJwt();
        Mockito.when(jwtDecoder.decode("test-token")).thenReturn(Mono.just(jwt));
    }
}
