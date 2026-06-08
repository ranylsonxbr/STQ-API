package com.example.Stq.relatorio.application.dto;

import java.util.List;

public record RelatorioMovimentacoes(List<MovimentacaoItem> dados, List<TotalPorTipo> totais) {}
