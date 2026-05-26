package com.example.Stq.autenticacao;

import com.example.Stq.autenticacao.application.AutenticacaoController;
import com.example.Stq.autenticacao.application.AutenticacaoService;
import com.example.Stq.autenticacao.application.dto.LoginRequest;
import com.example.Stq.autenticacao.application.dto.LoginResponse;
import com.example.Stq.autenticacao.application.dto.RefreshRequest;
import com.example.Stq.autenticacao.domain.exception.ContaDesativadaException;
import com.example.Stq.autenticacao.domain.exception.CredenciaisInvalidasException;
import com.example.Stq.config.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AutenticacaoControllerTest {

    @Mock
    private AutenticacaoService autenticacaoService;

    @InjectMocks
    private AutenticacaoController controller;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Nested
    @DisplayName("POST /api/auth/login")
    class Login {

        @Test
        @DisplayName("[AUTH-C1] deve retornar 200 com tokens quando credenciais são válidas")
        void deveRetornar200ComTokens() throws Exception {
            when(autenticacaoService.login(any()))
                    .thenReturn(new LoginResponse("access", "refresh", 28800L));

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    new LoginRequest("u@e.com", "senha"))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").value("access"))
                    .andExpect(jsonPath("$.refreshToken").value("refresh"))
                    .andExpect(jsonPath("$.expiresIn").value(28800));
        }

        @Test
        @DisplayName("[AUTH-C2] deve retornar 401 quando credenciais são inválidas")
        void deveRetornar401QuandoCredenciaisInvalidas() throws Exception {
            when(autenticacaoService.login(any())).thenThrow(new CredenciaisInvalidasException());

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    new LoginRequest("u@e.com", "errada"))))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.detail").value("Credenciais inválidas."));
        }

        @Test
        @DisplayName("[AUTH-C3] deve retornar 403 quando conta está desativada")
        void deveRetornar403QuandoContaDesativada() throws Exception {
            when(autenticacaoService.login(any())).thenThrow(new ContaDesativadaException());

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    new LoginRequest("u@e.com", "senha"))))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.detail").value("Conta desativada."));
        }

        @Test
        @DisplayName("deve retornar 400 quando payload é inválido")
        void deveRetornar400QuandoPayloadInvalido() throws Exception {
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"email\":\"\",\"senha\":\"\"}"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("POST /api/auth/refresh")
    class Refresh {

        @Test
        @DisplayName("[AUTH-C4] deve retornar 200 com novo accessToken")
        void deveRetornar200ComNovoAccessToken() throws Exception {
            when(autenticacaoService.refresh(any()))
                    .thenReturn(new LoginResponse("novo-access", "refresh", 28800L));

            mockMvc.perform(post("/api/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    new RefreshRequest("refresh"))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").value("novo-access"));
        }
    }
}
