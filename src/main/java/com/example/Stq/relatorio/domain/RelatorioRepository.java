package com.example.Stq.relatorio.domain;

import com.example.Stq.movimentacao.domain.TipoMovimentacao;
import com.example.Stq.pedidocompra.domain.StatusPedido;
import com.example.Stq.relatorio.application.dto.EstoqueAtualItem;
import com.example.Stq.relatorio.application.dto.MovimentacaoItem;
import com.example.Stq.relatorio.application.dto.PedidoCompraItem;
import com.example.Stq.relatorio.application.dto.TotalPorTipo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface RelatorioRepository {
    List<EstoqueAtualItem> estoqueAtual();
    List<EstoqueAtualItem> alertas();
    List<MovimentacaoItem> movimentacoesPorPeriodo(LocalDate de, LocalDate ate, TipoMovimentacao tipo);
    List<TotalPorTipo> totaisPorTipo(LocalDate de, LocalDate ate, TipoMovimentacao tipo);
    List<PedidoCompraItem> pedidosPorPeriodo(LocalDate de, LocalDate ate, StatusPedido status);
    BigDecimal totalGasto(LocalDate de, LocalDate ate);
}
