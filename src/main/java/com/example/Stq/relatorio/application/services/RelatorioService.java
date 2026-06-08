package com.example.Stq.relatorio.application.services;

import com.example.Stq.movimentacao.domain.TipoMovimentacao;
import com.example.Stq.pedidocompra.domain.StatusPedido;
import com.example.Stq.relatorio.application.dto.EstoqueAtualItem;
import com.example.Stq.relatorio.application.dto.RelatorioPedidos;
import com.example.Stq.relatorio.application.dto.RelatorioMovimentacoes;

import java.time.LocalDate;
import java.util.List;

public interface RelatorioService {
    List<EstoqueAtualItem> estoqueAtual();
    List<EstoqueAtualItem> alertas();
    RelatorioMovimentacoes movimentacoes(LocalDate de, LocalDate ate, TipoMovimentacao tipo);
    RelatorioPedidos pedidos(LocalDate de, LocalDate ate, StatusPedido status);
}
