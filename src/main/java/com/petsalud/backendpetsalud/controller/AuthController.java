package com.petsalud.backendpetsalud.controller;

import com.petsalud.backendpetsalud.security.JwtUtil;
import com.petsalud.backendpetsalud.security.SecurityResponses;
import com.petsalud.backendpetsalud.security.UsuarioDetails;
import org.springframework.context.annotation.Profile;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

@RestController
@Profile("!semana1")
public class AuthController {
    private final AuthenticationManager authentication;
    private final JwtUtil jwt;
    private final SecurityResponses responses;

    public AuthController(AuthenticationManager authentication, JwtUtil jwt, SecurityResponses responses) {
        this.authentication = authentication;
        this.jwt = jwt;
        this.responses = responses;
    }

    @PostMapping(path = "/api/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<JwtUtil.Token> login(@RequestBody Credenciales credenciales) {
        try {
            if (credenciales.email() == null || credenciales.email().length() > 254
                    || credenciales.password() == null
                    || credenciales.password().getBytes(StandardCharsets.UTF_8).length > 72) {
                throw new org.springframework.security.authentication.BadCredentialsException("Credenciales inválidas");
            }
            String email = credenciales.email().strip().toLowerCase(Locale.ROOT);
            var result = authentication.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(email, credenciales.password()));
            return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                    .body(jwt.emitir((UsuarioDetails) result.getPrincipal()));
        } catch (AuthenticationException ex) {
            responses.loginFallido();
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas");
        }
    }

    public record Credenciales(String email, String password) {}
}
