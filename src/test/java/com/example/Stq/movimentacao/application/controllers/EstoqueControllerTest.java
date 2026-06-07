package com.example.Stq.movimentacao.application.controllers;

import com.example.Stq.autenticacao.domain.Perfil;
import com.example.Stq.autenticacao.domain.Usuario;
import com.example.Stq.autenticacao.infra.UsuarioDetails;
import com.example.Stq.config.GlobalExceptionHandler;
import com.example.Stq.movimentacao.application.dto.EstoqueResponse;
import com.example.Stq.movimentacao.application.services.EstoqueService;
import com.example.Stq.movimentacao.domain.EstoqueFiltro;
import com.example.Stq.movimentacao.domain.StatusEstoque;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("EstoqueController")
class EstoqueControllerTest {

    @Mock
    private EstoqueService estoqueService;

    @InjectMocks
    private EstoqueController controller;

    private MockMvc mockMvc;

    private static final UUID PRODUTO_ID = UUID.fromString("00000000-0000-0000-0000-0000000000a1");

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(
                        new PageableHandlerMethodArgumentResolver(),
                        new AuthenticationPrincipalArgumentResolver())
                .build();
    }

    private void autenticarComo(Perfil perfil) {
        Usuario usuario = Usuario.builder()
                .id(UUID.randomUUID()).nome("Teste").email("teste@email.com")
                .senha("hash").perfil(perfil).ativo(true).build();
        UsuarioDetails details = new UsuarioDetails(usuario);
        var auth = new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    @DisplayName("[ESTQ-A1] VISUALIZADOR deve consultar saldo com status calculado")
    void visualizadorDeveConsultarSaldoComStatus() throws Exception {
        autenticarComo(Perfil.VISUALIZADOR);
        var resposta = new EstoqueResponse(
                UUID.randomUUID(), PRODUTO_ID, "PRD-000001", null,
                "A1", 3, 5, StatusEstoque.ABAIXO_MINIMO, Instant.now());
        var page = new PageImpl<>(List.of(resposta), PageRequest.of(0, 20), 1);
        when(estoqueService.consultarSaldo(any(EstoqueFiltro.class), any())).thenReturn(page);

        mockMvc.perform(get("/api/estoques").param("produtoId", PRODUTO_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].status").value("ABAIXO_MINIMO"))
                .andExpect(jsonPath("$.content[0].saldoAtual").value(3));
    }
}
