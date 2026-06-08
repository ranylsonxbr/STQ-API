package com.example.Stq.relatorio.application.controllers;

import com.example.Stq.autenticacao.domain.Perfil;
import com.example.Stq.autenticacao.domain.Usuario;
import com.example.Stq.autenticacao.infra.UsuarioDetails;
import com.example.Stq.config.GlobalExceptionHandler;
import com.example.Stq.movimentacao.domain.StatusEstoque;
import com.example.Stq.relatorio.application.dto.EstoqueAtualItem;
import com.example.Stq.relatorio.application.dto.MovimentacaoItem;
import com.example.Stq.relatorio.application.dto.PedidoCompraItem;
import com.example.Stq.relatorio.application.dto.RelatorioMovimentacoes;
import com.example.Stq.relatorio.application.dto.RelatorioPedidos;
import com.example.Stq.relatorio.application.dto.TotalPorTipo;
import com.example.Stq.relatorio.application.services.RelatorioService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("RelatorioController")
class RelatorioControllerTest {

    @Mock private RelatorioService relatorioService;
    @InjectMocks private RelatorioController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();
        autenticar();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void autenticar() {
        Usuario usuario = Usuario.builder()
                .id(UUID.randomUUID()).nome("Teste").email("t@t.com")
                .senha("hash").perfil(Perfil.VISUALIZADOR).ativo(true).build();
        UsuarioDetails details = new UsuarioDetails(usuario);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities()));
    }

    private EstoqueAtualItem itemEstoque() {
        return new EstoqueAtualItem("PRD-001", "Produto A", "UN", 10, 5, StatusEstoque.NORMAL);
    }

    @Nested
    @DisplayName("GET /api/relatorios/estoque-atual")
    class EstoqueAtual {

        @Test
        @DisplayName("[REL-C1] deve retornar 200 com lista JSON")
        void deveRetornar200Json() throws Exception {
            when(relatorioService.estoqueAtual()).thenReturn(List.of(itemEstoque()));

            mockMvc.perform(get("/api/relatorios/estoque-atual"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].sku").value("PRD-001"))
                    .andExpect(jsonPath("$[0].status").value("NORMAL"));
        }

        @Test
        @DisplayName("[REL-C5] com Accept text/csv deve retornar CSV com Content-Disposition")
        void comAcceptCsvDeveRetornarCsv() throws Exception {
            when(relatorioService.estoqueAtual()).thenReturn(List.of(itemEstoque()));

            var resultado = mockMvc.perform(get("/api/relatorios/estoque-atual")
                            .header("Accept", "text/csv"))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Content-Disposition",
                            org.hamcrest.Matchers.containsString("relatorio-estoque-atual.csv")))
                    .andReturn();

            String body = resultado.getResponse().getContentAsString();
            assertThat(body).contains("SKU").contains("PRD-001");
        }
    }

    @Nested
    @DisplayName("GET /api/relatorios/alertas")
    class Alertas {

        @Test
        @DisplayName("[REL-C2] deve retornar 200 com lista de alertas JSON")
        void deveRetornar200Json() throws Exception {
            var alerta = new EstoqueAtualItem("PRD-002", "Produto B", "KG", 2, 10, StatusEstoque.ABAIXO_MINIMO);
            when(relatorioService.alertas()).thenReturn(List.of(alerta));

            mockMvc.perform(get("/api/relatorios/alertas"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].status").value("ABAIXO_MINIMO"));
        }

        @Test
        @DisplayName("[REL-C5] com Accept text/csv deve retornar arquivo CSV")
        void comAcceptCsvDeveRetornarCsv() throws Exception {
            when(relatorioService.alertas()).thenReturn(List.of());

            mockMvc.perform(get("/api/relatorios/alertas").header("Accept", "text/csv"))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Content-Disposition",
                            org.hamcrest.Matchers.containsString("relatorio-alertas.csv")));
        }
    }

    @Nested
    @DisplayName("GET /api/relatorios/movimentacoes")
    class Movimentacoes {

        @Test
        @DisplayName("[REL-C3] deve retornar 200 com RelatorioMovimentacoes JSON")
        void deveRetornar200Json() throws Exception {
            var mov = new MovimentacaoItem("PRD-001", "Produto A", "ENTRADA", "MANUAL", 5, Instant.now());
            var total = new TotalPorTipo("ENTRADA", 5L);
            when(relatorioService.movimentacoes(isNull(), isNull(), isNull()))
                    .thenReturn(new RelatorioMovimentacoes(List.of(mov), List.of(total)));

            mockMvc.perform(get("/api/relatorios/movimentacoes"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.dados[0].produtoSku").value("PRD-001"))
                    .andExpect(jsonPath("$.totais[0].tipo").value("ENTRADA"));
        }

        @Test
        @DisplayName("[REL-C5] com Accept text/csv deve retornar arquivo CSV")
        void comAcceptCsvDeveRetornarCsv() throws Exception {
            when(relatorioService.movimentacoes(any(), any(), any()))
                    .thenReturn(new RelatorioMovimentacoes(List.of(), List.of()));

            mockMvc.perform(get("/api/relatorios/movimentacoes").header("Accept", "text/csv"))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Content-Disposition",
                            org.hamcrest.Matchers.containsString("relatorio-movimentacoes.csv")));
        }
    }

    @Nested
    @DisplayName("GET /api/relatorios/pedidos-compra")
    class PedidosCompra {

        @Test
        @DisplayName("[REL-C4] deve retornar 200 com RelatorioPedidos JSON")
        void deveRetornar200Json() throws Exception {
            var pedido = new PedidoCompraItem(UUID.randomUUID(), "Fornecedor X",
                    "RECEBIDO", LocalDate.now(), new BigDecimal("100.00"));
            when(relatorioService.pedidos(isNull(), isNull(), isNull()))
                    .thenReturn(new RelatorioPedidos(List.of(pedido), new BigDecimal("100.00")));

            mockMvc.perform(get("/api/relatorios/pedidos-compra"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.dados[0].fornecedorNome").value("Fornecedor X"))
                    .andExpect(jsonPath("$.totalGasto").value(100.00));
        }

        @Test
        @DisplayName("[REL-C5] com Accept text/csv deve retornar arquivo CSV")
        void comAcceptCsvDeveRetornarCsv() throws Exception {
            when(relatorioService.pedidos(any(), any(), any()))
                    .thenReturn(new RelatorioPedidos(List.of(), BigDecimal.ZERO));

            mockMvc.perform(get("/api/relatorios/pedidos-compra").header("Accept", "text/csv"))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Content-Disposition",
                            org.hamcrest.Matchers.containsString("relatorio-pedidos-compra.csv")));
        }
    }
}
