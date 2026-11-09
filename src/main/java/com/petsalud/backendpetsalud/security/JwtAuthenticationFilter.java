package com.petsalud.backendpetsalud.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtUtil jwt;
    private final UsuarioDetailsService usuarios;
    private final SecurityResponses responses;

    public JwtAuthenticationFilter(JwtUtil jwt, UsuarioDetailsService usuarios, SecurityResponses responses) {
        this.jwt = jwt;
        this.usuarios = usuarios;
        this.responses = responses;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.regionMatches(true, 0, "Bearer ", 0, 7)) {
            try {
                String token = header.substring(7);
                if (token.length() > 4096) {
                    throw new IllegalArgumentException("Token demasiado largo");
                }
                Claims claims = jwt.validar(token);
                UsuarioDetails usuario = usuarios.loadUserByUsername(claims.getSubject());
                if (!usuario.getId().equals(claims.get("uid", Long.class))) {
                    throw new BadCredentialsException("Identidad inválida");
                }
                var authentication = UsernamePasswordAuthenticationToken.authenticated(
                        usuario, null, usuario.getAuthorities());
                var context = SecurityContextHolder.createEmptyContext();
                context.setAuthentication(authentication);
                SecurityContextHolder.setContext(context);
            } catch (JwtException | IllegalArgumentException | UsernameNotFoundException | BadCredentialsException ex) {
                SecurityContextHolder.clearContext();
                responses.noAutenticado(request, response, "jwt_invalido");
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
