package com.example.Stq.frontend;

import com.example.Stq.autenticacao.domain.Perfil;
import com.example.Stq.autenticacao.domain.Usuario;
import com.example.Stq.autenticacao.domain.UsuarioRepository;
import com.example.Stq.autenticacao.infra.UsuarioJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.logout;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("FrontendIntegrationTest")
class FrontendIntegrationTest {

    private static final String EMAIL = "web@teste.com";
    private static final String SENHA = "senha-web-123";

    @Autowired WebApplicationContext wac;
    @Autowired UsuarioJpaRepository usuarioJpaRepository;
    @Autowired PasswordEncoder passwordEncoder;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac)
                .apply(springSecurity())
                .build();
        usuarioJpaRepository.deleteAll();
        ((UsuarioRepository) usuarioJpaRepository).save(Usuario.builder()
                .nome("Usuario Web")
                .email(EMAIL)
                .senha(passwordEncoder.encode(SENHA))
                .perfil(Perfil.ADMIN)
                .ativo(true)
                .build());
    }

    @Nested
    @DisplayName("Autenticação por formulário (cadeia web)")
    class FormLogin {

        @Test
        @DisplayName("[FW-I1] login com credenciais válidas redireciona para /web/dashboard")
        void loginValido() throws Exception {
            mockMvc.perform(formLogin("/web/login").user("email", EMAIL).password("senha", SENHA))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/web/dashboard"))
                    .andExpect(authenticated().withUsername(EMAIL));
        }

        @Test
        @DisplayName("[FW-I2] login com senha inválida redireciona para /web/login?erro")
        void loginInvalido() throws Exception {
            mockMvc.perform(formLogin("/web/login").user("email", EMAIL).password("senha", "errada"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/web/login?erro"))
                    .andExpect(unauthenticated());
        }

        @Test
        @DisplayName("[FW-I3] logout invalida a sessão e redireciona para /web/login?logout")
        void logoutLimpaSessao() throws Exception {
            mockMvc.perform(logout("/web/logout"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/web/login?logout"))
                    .andExpect(unauthenticated());
        }
    }

    @Nested
    @DisplayName("Proteção de rotas web")
    class ProtecaoRotasWeb {

        @Test
        @DisplayName("[FW-I4] acesso a /web/dashboard sem sessão redireciona para /web/login")
        void dashboardSemSessao() throws Exception {
            mockMvc.perform(get("/web/dashboard"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrlPattern("**/web/login"));
        }

        @Test
        @DisplayName("[FW-I5] GET /web/login é público (200)")
        void loginPublico() throws Exception {
            mockMvc.perform(get("/web/login"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("Cadeia API permanece stateless (não afetada pela bifurcação)")
    class CadeiaApiIntacta {

        @Test
        @DisplayName("[FW-I6] GET /api/produtos sem JWT retorna 401 com ProblemDetail")
        void apiSemTokenRetorna401() throws Exception {
            mockMvc.perform(get("/api/produtos"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(content().contentTypeCompatibleWith("application/problem+json"));
        }

        @Test
        @DisplayName("[FW-I7] POST /api/produtos não exige CSRF (cadeia stateless), apenas autenticação")
        void apiNaoExigeCsrf() throws Exception {
            mockMvc.perform(post("/api/produtos").contentType("application/json").content("{}"))
                    .andExpect(status().isUnauthorized());
        }
    }
}
