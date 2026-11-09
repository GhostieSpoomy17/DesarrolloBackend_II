package com.petsalud.backendpetsalud;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:petsalud_semana1;MODE=MySQL")
@AutoConfigureMockMvc
@ActiveProfiles({"test", "semana1"})
class Semana1Tests {
    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper json;

    @Test
    void registroSinTokenCsrfSeRechaza() throws Exception {
        mvc.perform(post("/api/usuarios/registro").contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void registroConCsrfPermiteAutenticacionBasicConCorreo() throws Exception {
        var result = mvc.perform(get("/api/csrf")).andExpect(status().isOk()).andReturn().getResponse();
        var token = json.readTree(result.getContentAsString());
        Cookie cookie = result.getCookie("XSRF-TOKEN");
        mvc.perform(post("/api/usuarios/registro")
                        .cookie(cookie)
                        .header(token.get("headerName").asText(), token.get("token").asText())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"basica@petsalud.test","password":"Prueba-basica-123!"}
                                """))
                .andExpect(status().isCreated());
        mvc.perform(get("/api/productos").with(httpBasic("basica@petsalud.test", "Prueba-basica-123!")))
                .andExpect(status().isOk());
        mvc.perform(get("/api/productos").with(httpBasic("basica@petsalud.test", "equivocada")))
                .andExpect(status().isUnauthorized());
    }
}
