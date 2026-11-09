package com.petsalud.backendpetsalud.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class SecurityResponses {
    private static final Logger audit = LoggerFactory.getLogger("petsalud.seguridad");

    public void noAutenticado(HttpServletRequest request, HttpServletResponse response, String evento)
            throws IOException {
        audit.warn("evento={} metodo={}", evento, request.getMethod());
        response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
        escribir(response, 401, "Autenticación requerida o credenciales inválidas");
    }

    public void prohibido(HttpServletRequest request, HttpServletResponse response) throws IOException {
        audit.warn("evento=acceso_denegado metodo={}", request.getMethod());
        escribir(response, 403, "Acceso denegado");
    }

    public void loginFallido() {
        audit.warn("evento=login_fallido");
    }

    private void escribir(HttpServletResponse response, int status, String mensaje) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"error\":\"" + mensaje + "\"}");
    }
}
