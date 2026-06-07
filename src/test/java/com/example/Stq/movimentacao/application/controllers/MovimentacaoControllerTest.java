package com.example.Stq.movimentacao.application.controllers;

import com.example.Stq.autenticacao.domain.Perfil;
import com.example.Stq.autenticacao.domain.Usuario;
import com.example.Stq.autenticacao.infra.UsuarioDetails;
import com.example.Stq.config.GlobalExceptionHandler;
import com.example.Stq.movimentacao.application.dto.EntradaRequest;
import com.example.Stq.movimentacao.application.dto.MovimentacaoResponse;
import com.example.Stq.movimentacao.application.dto.SaidaRequest;
import com.example.Stq.movimentacao.application.services.MovimentacaoService;
import com.example.Stq.movimentacao.domain.MovimentacaoFiltro;
import com.example.Stq.movimentacao.domain.OrigemMovimentacao;
import com.example.Stq.movimentacao.domain.TipoMovimentacao;
import com.example.Stq.movimentacao.domain.exception.SaldoInsuficienteException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("MovimentacaoController")
class MovimentacaoControllerTest {

    @Mock
    private MovimentacaoService movimentacaoService;

    @InjectMocks
    private MovimentacaoController controller;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final UUID MOV_ID = UUID.fromString("00000000-0000-0000-0000-0000000000c1");
    private static final UUID PRODUTO_ID = UUID.fromString("00000000-0000-0000-0000-0000000000a1");
    private static final UUID USUARIO_ID = UUID.fromString("00000000-0000-0000-0000-0000000000b2");

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
                .id(USUARIO_ID).nome("Teste").email("teste@email.com")
                .senha("hash").perfil(perfil).ativo(true).build();
        UsuarioDetails details = new UsuarioDetails(usuario);
        var auth = new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private MovimentacaoResponse responseFixture() {
        return new MovimentacaoResponse(
                MOV_ID, PRODUTO_ID, "PRD-000001", null,
                TipoMovimentacao.ENTRADA, OrigemMovimentacao.MANUAL,
                7, 0, 7, "compra", USUARIO_ID, Instant.now());
    }

    @Nested
    @DisplayName("POST /api/movimentacoes/entrada")
    class Entrada {

        @Test
        @DisplayName("[MOV-C1] OPERADOR deve registrar entrada e retornar 201 com Location")
        void operadorDeveRegistrarEntradaERetornar201ComLocation() throws Exception {
            autenticarComo(Perfil.OPERADOR);
            when(movimentacaoService.registrarEntrada(any(EntradaRequest.class), eq(USUARIO_ID)))
                    .thenReturn(responseFixture());
            var req = new EntradaRequest(PRODUTO_ID, null, "A1", 7, "compra");

            mockMvc.perform(post("/api/movimentacoes/entrada")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location",
                            org.hamcrest.Matchers.containsString("/api/movimentacoes/" + MOV_ID)));
        }

        @Test
        @DisplayName("[MOV-A1] body inválido em entrada deve retornar 400 com application/problem+json")
        void bodyInvalidoDeveRetornar400() throws Exception {
            autenticarComo(Perfil.OPERADOR);
            var reqInvalido = new EntradaRequest(null, null, "A1", null, null);

            mockMvc.perform(post("/api/movimentacoes/entrada")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(reqInvalido)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400));
        }
    }

    @Nested
    @DisplayName("POST /api/movimentacoes/saida")
    class Saida {

        @Test
        @DisplayName("[MOV-C3] saída insuficiente deve retornar 422 com application/problem+json")
        void saidaInsuficienteDeveRetornar422() throws Exception {
            autenticarComo(Perfil.OPERADOR);
            when(movimentacaoService.registrarSaida(any(SaidaRequest.class), eq(USUARIO_ID)))
                    .thenThrow(new SaldoInsuficienteException(2, 5));
            var req = new SaidaRequest(PRODUTO_ID, null, "A1", 5, null);

            mockMvc.perform(post("/api/movimentacoes/saida")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.status").value(422));
        }
    }

    @Nested
    @DisplayName("GET /api/movimentacoes")
    class Listar {

        @Test
        @DisplayName("[MOV-C6] listar com filtros deve retornar 200 paginado")
        void listarComFiltrosDeveRetornar200() throws Exception {
            autenticarComo(Perfil.VISUALIZADOR);
            var page = new PageImpl<>(List.of(responseFixture()), PageRequest.of(0, 20), 1);
            when(movimentacaoService.listar(any(MovimentacaoFiltro.class), any())).thenReturn(page);

            mockMvc.perform(get("/api/movimentacoes").param("tipo", "ENTRADA"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content[0].tipo").value("ENTRADA"));

            verify(movimentacaoService).listar(any(MovimentacaoFiltro.class), any());
        }
    }

    @Nested
    @DisplayName("PUT /api/movimentacoes")
    class Imutabilidade {

        @Test
        @DisplayName("[MOV-C7] PUT em /api/movimentacoes deve retornar 405")
        void putDeveRetornar405() throws Exception {
            autenticarComo(Perfil.OPERADOR);

            mockMvc.perform(put("/api/movimentacoes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isMethodNotAllowed())
                    .andExpect(jsonPath("$.status").value(405));
        }
    }
}
