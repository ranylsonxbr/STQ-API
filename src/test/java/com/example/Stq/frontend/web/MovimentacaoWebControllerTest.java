package com.example.Stq.frontend.web;

import com.example.Stq.frontend.support.UsuarioLogado;
import com.example.Stq.movimentacao.application.dto.EstoqueResponse;
import com.example.Stq.movimentacao.application.dto.MovimentacaoResponse;
import com.example.Stq.movimentacao.application.services.EstoqueService;
import com.example.Stq.movimentacao.application.services.MovimentacaoService;
import com.example.Stq.movimentacao.domain.OrigemMovimentacao;
import com.example.Stq.movimentacao.domain.StatusEstoque;
import com.example.Stq.movimentacao.domain.TipoMovimentacao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@ExtendWith(MockitoExtension.class)
@DisplayName("MovimentacaoWebController")
class MovimentacaoWebControllerTest {

    @Mock
    private MovimentacaoService movimentacaoService;
    @Mock
    private EstoqueService estoqueService;
    @Mock
    private UsuarioLogado usuarioLogado;

    @InjectMocks
    private MovimentacaoWebController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private UUID randomId() {
        return UUID.randomUUID();
    }

    private MovimentacaoResponse movimentacaoResponse() {
        return new MovimentacaoResponse(randomId(), randomId(), "PRD-000001",
                null, TipoMovimentacao.ENTRADA, OrigemMovimentacao.MANUAL,
                10, 0, 10, "obs", randomId(), Instant.now());
    }

    private EstoqueResponse estoqueResponse() {
        return new EstoqueResponse(randomId(), randomId(), "PRD-000001",
                null, "A1", 20, 5, StatusEstoque.NORMAL, Instant.now());
    }

    private Authentication authMock(UUID usuarioId) {
        Authentication auth = mock(Authentication.class);
        when(usuarioLogado.obterUsuarioId(auth)).thenReturn(usuarioId);
        return auth;
    }

    // -------------------------------------------------------------------------
    // GET /web/movimentacoes
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("GET /web/movimentacoes")
    class Listar {

        @Test
        @DisplayName("[FW-M1] deve retornar view 'movimentacao/lista' com atributo 'movimentacoes'")
        void deveRetornarListaComMovimentacoes() throws Exception {
            // Arrange
            Page<MovimentacaoResponse> page = new PageImpl<>(List.of(movimentacaoResponse()));
            when(movimentacaoService.listar(any(), any())).thenReturn(page);

            // Act + Assert
            mockMvc.perform(get("/web/movimentacoes"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("movimentacao/lista"))
                    .andExpect(model().attributeExists("movimentacoes"));
        }
    }

    // -------------------------------------------------------------------------
    // GET /web/estoque
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("GET /web/estoque")
    class Estoque {

        @Test
        @DisplayName("[FW-M2] deve retornar view 'movimentacao/estoque' com atributo 'estoques'")
        void deveRetornarEstoque() throws Exception {
            // Arrange
            Page<EstoqueResponse> page = new PageImpl<>(List.of(estoqueResponse()));
            when(estoqueService.consultarSaldo(any(), any())).thenReturn(page);

            // Act + Assert
            mockMvc.perform(get("/web/estoque"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("movimentacao/estoque"))
                    .andExpect(model().attributeExists("estoques"));
        }
    }

    // -------------------------------------------------------------------------
    // GET /web/movimentacoes/entrada
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("GET /web/movimentacoes/entrada")
    class EntradaForm {

        @Test
        @DisplayName("[FW-M3] deve retornar view 'movimentacao/entrada' com entradaForm")
        void deveRetornarFormEntrada() throws Exception {
            mockMvc.perform(get("/web/movimentacoes/entrada"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("movimentacao/entrada"))
                    .andExpect(model().attributeExists("entradaForm"));
        }
    }

    // -------------------------------------------------------------------------
    // POST /web/movimentacoes/entrada
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("POST /web/movimentacoes/entrada")
    class RegistrarEntrada {

        @Test
        @DisplayName("[FW-M4] deve redirecionar com flash sucesso apos registrar entrada valida")
        void deveRedirecionarComFlashSucessoAposEntrada() throws Exception {
            // Arrange
            UUID usuarioId = randomId();
            Authentication auth = authMock(usuarioId);
            when(movimentacaoService.registrarEntrada(any(), eq(usuarioId)))
                    .thenReturn(movimentacaoResponse());

            // Act + Assert
            mockMvc.perform(post("/web/movimentacoes/entrada")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("produtoId", randomId().toString())
                            .param("quantidade", "10")
                            .param("observacao", "Entrada teste")
                            .principal(auth))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/web/movimentacoes"))
                    .andExpect(flash().attribute("sucesso", "Entrada registrada com sucesso."));

            verify(movimentacaoService).registrarEntrada(any(), eq(usuarioId));
        }

        @Test
        @DisplayName("[FW-M5] deve retornar form quando dados de entrada invalidos")
        void deveRetornarFormQuandoDadosInvalidos() throws Exception {
            // Act + Assert — produtoId null e quantidade null disparam @NotNull/@Positive
            mockMvc.perform(post("/web/movimentacoes/entrada")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("quantidade", "-1"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("movimentacao/entrada"));
        }
    }

    // -------------------------------------------------------------------------
    // GET /web/movimentacoes/saida
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("GET /web/movimentacoes/saida")
    class SaidaForm {

        @Test
        @DisplayName("[FW-M6] deve retornar view 'movimentacao/saida' com saidaForm")
        void deveRetornarFormSaida() throws Exception {
            mockMvc.perform(get("/web/movimentacoes/saida"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("movimentacao/saida"))
                    .andExpect(model().attributeExists("saidaForm"));
        }
    }

    // -------------------------------------------------------------------------
    // POST /web/movimentacoes/saida
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("POST /web/movimentacoes/saida")
    class RegistrarSaida {

        @Test
        @DisplayName("[FW-M7] deve redirecionar com flash sucesso apos registrar saida valida")
        void deveRedirecionarComFlashSucessoAposSaida() throws Exception {
            // Arrange
            UUID usuarioId = randomId();
            Authentication auth = authMock(usuarioId);
            when(movimentacaoService.registrarSaida(any(), eq(usuarioId)))
                    .thenReturn(movimentacaoResponse());

            // Act + Assert
            mockMvc.perform(post("/web/movimentacoes/saida")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("produtoId", randomId().toString())
                            .param("quantidade", "5")
                            .param("observacao", "Saida teste")
                            .principal(auth))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/web/movimentacoes"))
                    .andExpect(flash().attribute("sucesso", "Saída registrada com sucesso."));
        }

        @Test
        @DisplayName("[FW-M8] deve redirecionar com flash erro quando RuntimeException (RN-01 saldo insuficiente)")
        void deveRedirecionarComFlashErroQuandoSaldoInsuficiente() throws Exception {
            // Arrange
            UUID usuarioId = randomId();
            Authentication auth = authMock(usuarioId);
            when(movimentacaoService.registrarSaida(any(), eq(usuarioId)))
                    .thenThrow(new RuntimeException("Saldo insuficiente para saída."));

            // Act + Assert
            mockMvc.perform(post("/web/movimentacoes/saida")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("produtoId", randomId().toString())
                            .param("quantidade", "9999")
                            .principal(auth))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/web/movimentacoes"))
                    .andExpect(flash().attribute("erro", "Saldo insuficiente para saída."));
        }

        @Test
        @DisplayName("[FW-M9] deve retornar form quando dados de saida invalidos")
        void deveRetornarFormQuandoDadosInvalidos() throws Exception {
            // Act + Assert — produtoId ausente e quantidade zero (não é @Positive)
            mockMvc.perform(post("/web/movimentacoes/saida")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("quantidade", "0"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("movimentacao/saida"));
        }
    }

    // -------------------------------------------------------------------------
    // GET /web/movimentacoes/transferencia
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("GET /web/movimentacoes/transferencia")
    class TransferenciaForm {

        @Test
        @DisplayName("[FW-M10] deve retornar view 'movimentacao/transferencia' com transferenciaForm")
        void deveRetornarFormTransferencia() throws Exception {
            mockMvc.perform(get("/web/movimentacoes/transferencia"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("movimentacao/transferencia"))
                    .andExpect(model().attributeExists("transferenciaForm"));
        }
    }

    // -------------------------------------------------------------------------
    // POST /web/movimentacoes/transferencia
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("POST /web/movimentacoes/transferencia")
    class RegistrarTransferencia {

        @Test
        @DisplayName("[FW-M11] deve redirecionar com flash sucesso apos registrar transferencia valida")
        void deveRedirecionarComFlashSucessoAposTransferencia() throws Exception {
            // Arrange
            UUID usuarioId = randomId();
            Authentication auth = authMock(usuarioId);
            when(movimentacaoService.registrarTransferencia(any(), eq(usuarioId)))
                    .thenReturn(movimentacaoResponse());

            // Act + Assert
            mockMvc.perform(post("/web/movimentacoes/transferencia")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("produtoId", randomId().toString())
                            .param("localizacaoOrigem", "A1")
                            .param("localizacaoDestino", "B2")
                            .param("quantidade", "3")
                            .principal(auth))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/web/movimentacoes"))
                    .andExpect(flash().attribute("sucesso", "Transferência registrada com sucesso."));
        }

        @Test
        @DisplayName("[FW-M12] deve retornar form quando dados de transferencia invalidos")
        void deveRetornarFormQuandoDadosInvalidos() throws Exception {
            // Act + Assert — localizacaoOrigem em branco viola @NotBlank
            mockMvc.perform(post("/web/movimentacoes/transferencia")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("produtoId", randomId().toString())
                            .param("localizacaoOrigem", "")
                            .param("localizacaoDestino", "B2")
                            .param("quantidade", "3"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("movimentacao/transferencia"));
        }
    }

    // -------------------------------------------------------------------------
    // GET /web/movimentacoes/ajuste
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("GET /web/movimentacoes/ajuste")
    class AjusteForm {

        @Test
        @DisplayName("[FW-M13] deve retornar view 'movimentacao/ajuste' com ajusteForm")
        void deveRetornarFormAjuste() throws Exception {
            mockMvc.perform(get("/web/movimentacoes/ajuste"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("movimentacao/ajuste"))
                    .andExpect(model().attributeExists("ajusteForm"));
        }
    }

    // -------------------------------------------------------------------------
    // POST /web/movimentacoes/ajuste
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("POST /web/movimentacoes/ajuste")
    class RegistrarAjuste {

        @Test
        @DisplayName("[FW-M14] deve redirecionar com flash sucesso apos registrar ajuste valido (RN-09)")
        void deveRedirecionarComFlashSucessoAposAjuste() throws Exception {
            // Arrange
            UUID usuarioId = randomId();
            Authentication auth = authMock(usuarioId);
            when(movimentacaoService.registrarAjuste(any(), eq(usuarioId)))
                    .thenReturn(movimentacaoResponse());

            // Act + Assert
            mockMvc.perform(post("/web/movimentacoes/ajuste")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("produtoId", randomId().toString())
                            .param("delta", "-3")
                            .param("observacao", "Ajuste de inventário")
                            .principal(auth))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/web/movimentacoes"))
                    .andExpect(flash().attribute("sucesso", "Ajuste de inventário registrado com sucesso."));

            verify(movimentacaoService).registrarAjuste(any(), eq(usuarioId));
        }

        @Test
        @DisplayName("[FW-M15] deve retornar form quando dados de ajuste invalidos")
        void deveRetornarFormQuandoDadosInvalidos() throws Exception {
            // Act + Assert — produtoId ausente viola @NotNull; delta ausente viola @NotNull
            mockMvc.perform(post("/web/movimentacoes/ajuste")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("observacao", "sem produto"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("movimentacao/ajuste"));
        }
    }
}
