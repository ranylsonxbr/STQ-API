package com.example.Stq.relatorio.application.services;

import com.example.Stq.movimentacao.domain.TipoMovimentacao;
import com.example.Stq.pedidocompra.domain.StatusPedido;
import com.example.Stq.relatorio.application.dto.EstoqueAtualItem;
import com.example.Stq.relatorio.application.dto.RelatorioPedidos;
import com.example.Stq.relatorio.application.dto.RelatorioMovimentacoes;
import com.example.Stq.relatorio.domain.RelatorioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RelatorioServiceImpl implements RelatorioService {

    private final RelatorioRepository relatorioRepository;

    @Override
    public List<EstoqueAtualItem> estoqueAtual() {
        return relatorioRepository.estoqueAtual();
    }

    @Override
    public List<EstoqueAtualItem> alertas() {
        return relatorioRepository.alertas();
    }

    @Override
    public RelatorioMovimentacoes movimentacoes(LocalDate de, LocalDate ate, TipoMovimentacao tipo) {
        var dados = relatorioRepository.movimentacoesPorPeriodo(de, ate, tipo);
        var totais = relatorioRepository.totaisPorTipo(de, ate, tipo);
        return new RelatorioMovimentacoes(dados, totais);
    }

    @Override
    public RelatorioPedidos pedidos(LocalDate de, LocalDate ate, StatusPedido status) {
        var dados = relatorioRepository.pedidosPorPeriodo(de, ate, status);
        var totalGasto = relatorioRepository.totalGasto(de, ate);
        return new RelatorioPedidos(dados, totalGasto);
    }
}
