package com.petsalud.backendpetsalud;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petsalud.backendpetsalud.dto.RegistroUsuario;
import com.petsalud.backendpetsalud.entity.Mascota;
import com.petsalud.backendpetsalud.entity.Rol;
import com.petsalud.backendpetsalud.entity.Usuario;
import com.petsalud.backendpetsalud.repository.MascotaRepository;
import com.petsalud.backendpetsalud.repository.UsuarioRepository;
import com.petsalud.backendpetsalud.service.UsuarioServiceImpl;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.Instant;
import java.util.Date;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BackendpetsaludApplicationTests {
    private static final String PASSWORD = "Clave-solo-pruebas-123!";
    private static final String CLIENTE = "cliente@petsalud.test";
    private static final String EMPLEADO = "empleado@petsalud.test";
    private static final String GERENTE = "gerente@petsalud.test";
    private static final String PRODUCTO = """
            {"nombre":"Alimento de prueba","categoria":"Alimento","precio":1000,"stock":10}
            """;

    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper json;
    @Autowired private UsuarioRepository usuarios;
    @Autowired private MascotaRepository mascotas;
    @Autowired private UsuarioServiceImpl registro;
    @Autowired private PasswordEncoder encoder;
    @Value("${petsalud.jwt.secret}") private String jwtSecret;
    private Usuario cliente;
    private Usuario otroCliente;
    private Mascota mascotaAjena;

    @BeforeAll
    void prepararCuentas() {
        cliente = cuenta(CLIENTE, Rol.CLIENTE);
        otroCliente = cuenta("otra@petsalud.test", Rol.CLIENTE);
        cuenta(EMPLEADO, Rol.EMPLEADO);
        cuenta(GERENTE, Rol.GERENTE);
        cuenta("veterinario-anterior@petsalud.test", Rol.VETERINARIO);
        mascotaAjena = mascotas.save(Mascota.builder().nombre("Luna").propietario(otroCliente).build());
    }

    @Test
    void registroSiempreCreaClienteYGuardaBcryptSinExponerlo() throws Exception {
        mvc.perform(post("/api/usuarios/registro").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("email", "Nuevo@petsalud.test",
                                "password", PASSWORD, "rol", "GERENTE", "id", 999))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rol").value("CLIENTE"))
                .andExpect(jsonPath("$.password").doesNotExist());
        Usuario nuevo = usuarios.findByEmail("nuevo@petsalud.test").orElseThrow();
        assertThat(nuevo.getId()).isNotEqualTo(999L);
        assertThat(nuevo.getPassword()).isNotEqualTo(PASSWORD);
        assertThat(encoder.matches(PASSWORD, nuevo.getPassword())).isTrue();
    }

    @Test
    void correoDuplicadoSeRechazaInclusoConMayusculas() throws Exception {
        mvc.perform(post("/api/usuarios/registro").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("email", CLIENTE.toUpperCase(), "password", PASSWORD))))
                .andExpect(status().isConflict());
    }

    @Test
    void registroRechazaClaveCorta() throws Exception {
        mvc.perform(post("/api/usuarios/registro").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"corta@petsalud.test\",\"password\":\"123\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void loginCorrectoEmiteTokenSinCookie() throws Exception {
        login(CLIENTE, PASSWORD)
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(header().doesNotExist("Set-Cookie"))
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(900));
    }

    @Test
    void loginConClaveIncorrectaDevuelve401() throws Exception {
        login(CLIENTE, "incorrecta").andExpect(status().isUnauthorized());
    }

    @Test
    void solicitudSinTokenDevuelve401() throws Exception {
        mvc.perform(get("/api/productos")).andExpect(status().isUnauthorized());
    }

    @Test
    void perfilFinalRechazaAutenticacionBasic() throws Exception {
        mvc.perform(get("/api/productos").with(httpBasic(CLIENTE, PASSWORD)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tokenManipuladoSeRechaza() throws Exception {
        String token = token(CLIENTE);
        int signature = token.lastIndexOf('.') + 1;
        String altered = token.substring(0, signature) + (token.charAt(signature) == 'A' ? "B" : "A")
                + token.substring(signature + 1);
        mvc.perform(get("/api/productos").header("Authorization", "Bearer " + altered))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tokenVencidoSeRechazaAunqueLaFirmaSeaValida() throws Exception {
        String expired = Jwts.builder().issuer("petsalud").audience().add("petsalud-api").and()
                .subject(CLIENTE).claim("uid", cliente.getId())
                .expiration(Date.from(Instant.now().minusSeconds(30)))
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret)), Jwts.SIG.HS256).compact();
        mvc.perform(get("/api/productos").header("Authorization", "Bearer " + expired))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void clientePuedeLeerCatalogoYSuPerfilPeroNoOtrosUsuarios() throws Exception {
        String header = "Bearer " + token(CLIENTE);
        mvc.perform(get("/api/productos").header("Authorization", header)).andExpect(status().isOk());
        mvc.perform(get("/api/usuarios/{id}", cliente.getId()).header("Authorization", header))
                .andExpect(status().isOk()).andExpect(jsonPath("$.password").doesNotExist());
        mvc.perform(get("/api/usuarios/{id}", otroCliente.getId()).header("Authorization", header))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/mascotas/{id}", mascotaAjena.getId()).header("Authorization", header))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/productos").header("Authorization", header)
                        .contentType(MediaType.APPLICATION_JSON).content(PRODUCTO))
                .andExpect(status().isForbidden());
    }

    @Test
    void empleadoPuedeCrearProductoPeroNoListarUsuariosNiEliminarProducto() throws Exception {
        String header = "Bearer " + token(EMPLEADO);
        String body = mvc.perform(post("/api/productos").header("Authorization", header)
                        .contentType(MediaType.APPLICATION_JSON).content(PRODUCTO))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        mvc.perform(get("/api/usuarios").header("Authorization", header)).andExpect(status().isForbidden());
        mvc.perform(delete("/api/productos/{id}", json.readTree(body).get("id").asLong())
                        .header("Authorization", header)).andExpect(status().isForbidden());
    }

    @Test
    void gerentePuedeListarUsuariosSinVerCredenciales() throws Exception {
        mvc.perform(get("/api/usuarios").header("Authorization", "Bearer " + token(GERENTE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].password").doesNotExist());
    }

    @Test
    void rolHistoricoVeterinarioMantieneAccesoDeEmpleado() throws Exception {
        mvc.perform(get("/api/productos/stock-bajo").param("cantidad", "20")
                        .header("Authorization", "Bearer " + token("veterinario-anterior@petsalud.test")))
                .andExpect(status().isOk());
    }

    @Test
    void tokenVigenteUsaElRolActualDeLaCuenta() throws Exception {
        Usuario cuenta = cuenta("cambio@petsalud.test", Rol.GERENTE);
        String header = "Bearer " + token(cuenta.getEmail());
        mvc.perform(get("/api/usuarios").header("Authorization", header)).andExpect(status().isOk());
        cuenta.setRol(Rol.CLIENTE);
        usuarios.saveAndFlush(cuenta);
        mvc.perform(get("/api/usuarios").header("Authorization", header)).andExpect(status().isForbidden());
    }

    private Usuario cuenta(String email, Rol rol) {
        return registro.crearCuenta(new RegistroUsuario(email, PASSWORD, "Prueba", null, null, null), rol);
    }

    private String token(String email) throws Exception {
        String body = login(email, PASSWORD).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return json.readTree(body).get("accessToken").asText();
    }

    private ResultActions login(String email, String password) throws Exception {
        return mvc.perform(post("/api/login").contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("email", email, "password", password))));
    }
}
