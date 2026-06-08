package com.example.Stq.relatorio.application.services;

import com.example.Stq.movimentacao.domain.StatusEstoque;
import com.example.Stq.movimentacao.domain.TipoMovimentacao;
import com.example.Stq.pedidocompra.domain.StatusPedido;
import com.example.Stq.relatorio.application.dto.EstoqueAtualItem;
import com.example.Stq.relatorio.application.dto.MovimentacaoItem;
import com.example.Stq.relatorio.application.dto.PedidoCompraItem;
import com.example.Stq.relatorio.application.dto.RelatorioMovimentacoes;
import com.example.Stq.relatorio.application.dto.RelatorioPedidos;
import com.example.Stq.relatorio.application.dto.TotalPorTipo;
import com.example.Stq.relatorio.domain.RelatorioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("RelatorioService")
class RelatorioServiceTest {

    @Mock private RelatorioRepository relatorioRepository;
    @InjectMocks private RelatorioServiceImpl service;

    private EstoqueAtualItem itemEstoque(String sku, int saldo, int minimo, StatusEstoque status) {
        return new EstoqueAtualItem(sku, "Produto " + sku, "UN", saldo, minimo, status);
    }

    @Nested
    @DisplayName("estoqueAtual")
    class EstoqueAtual {

        @Test
        @DisplayName("[REL-C1] deve delegar ao repositório e retornar lista")
        void deveDelegarAoRepositorio() {
            var lista = List.of(itemEstoque("PRD-001", 10, 5, StatusEstoque.NORMAL));
            when(relatorioRepository.estoqueAtual()).thenReturn(lista);

            var resultado = service.estoqueAtual();

            assertThat(resultado).hasSize(1);
            assertThat(resultado.get(0).sku()).isEqualTo("PRD-001");
            assertThat(resultado.get(0).status()).isEqualTo(StatusEstoque.NORMAL);
        }
    }

    @Nested
    @DisplayName("alertas")
    class Alertas {

        @Test
        @DisplayName("[REL-C2] deve retornar apenas itens abaixo do mínimo")
        void deveRetornarApenasItensAbaixoDoMinimo() {
            var lista = List.of(itemEstoque("PRD-002", 2, 5, StatusEstoque.ABAIXO_MINIMO));
            when(relatorioRepository.alertas()).thenReturn(lista);

            var resultado = service.alertas();

            assertThat(resultado).hasSize(1);
            assertThat(resultado.get(0).status()).isEqualTo(StatusEstoque.ABAIXO_MINIMO);
        }
    }

    @Nested
    @DisplayName("movimentacoes")
    class Movimentacoes {

        @Test
        @DisplayName("[REL-C3] deve montar RelatorioMovimentacoes com dados e totais")
        void deveMontarRelatorioComDadosETotais() {
            var mov = new MovimentacaoItem("PRD-001", "Produto A", "ENTRADA", "MANUAL", 5, Instant.now());
            var total = new TotalPorTipo("ENTRADA", 5L);
            when(relatorioRepository.movimentacoesPorPeriodo(null, null, null)).thenReturn(List.of(mov));
            when(relatorioRepository.totaisPorTipo(null, null, null)).thenReturn(List.of(total));

            RelatorioMovimentacoes resultado = service.movimentacoes(null, null, null);

            assertThat(resultado.dados()).hasSize(1);
            assertThat(resultado.totais()).hasSize(1);
            assertThat(resultado.totais().get(0).tipo()).isEqualTo("ENTRADA");
            assertThat(resultado.totais().get(0).quantidade()).isEqualTo(5L);
        }

        @Test
        @DisplayName("[REL-C3] deve passar filtros de período e tipo ao repositório")
        void devePassarFiltrosAoRepositorio() {
            LocalDate de = LocalDate.of(2026, 1, 1);
            LocalDate ate = LocalDate.of(2026, 1, 31);
            when(relatorioRepository.movimentacoesPorPeriodo(de, ate, TipoMovimentacao.ENTRADA)).thenReturn(List.of());
            when(relatorioRepository.totaisPorTipo(de, ate, TipoMovimentacao.ENTRADA)).thenReturn(List.of());

            RelatorioMovimentacoes resultado = service.movimentacoes(de, ate, TipoMovimentacao.ENTRADA);

            assertThat(resultado.dados()).isEmpty();
            assertThat(resultado.totais()).isEmpty();
        }
    }

    @Nested
    @DisplayName("pedidos")
    class Pedidos {

        @Test
        @DisplayName("[REL-C4] deve montar RelatorioPedidos com dados e totalGasto")
        void deveMontarRelatorioComDadosETotalGasto() {
            var item = new PedidoCompraItem(UUID.randomUUID(), "Fornecedor X", "RECEBIDO",
                    LocalDate.now(), new BigDecimal("500.00"));
            when(relatorioRepository.pedidosPorPeriodo(null, null, null)).thenReturn(List.of(item));
            when(relatorioRepository.totalGasto(null, null)).thenReturn(new BigDecimal("500.00"));

            RelatorioPedidos resultado = service.pedidos(null, null, null);

            assertThat(resultado.dados()).hasSize(1);
            assertThat(resultado.totalGasto()).isEqualByComparingTo("500.00");
        }

        @Test
        @DisplayName("[REL-C4] totalGasto considera apenas pedidos RECEBIDO")
        void totalGastoApenasPedidosRecebidos() {
            when(relatorioRepository.pedidosPorPeriodo(null, null, StatusPedido.PENDENTE)).thenReturn(List.of());
            when(relatorioRepository.totalGasto(null, null)).thenReturn(BigDecimal.ZERO);

            RelatorioPedidos resultado = service.pedidos(null, null, StatusPedido.PENDENTE);

            assertThat(resultado.totalGasto()).isEqualByComparingTo("0");
        }
    }
}
