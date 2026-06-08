package com.example.Stq.relatorio.application.dto;

import com.example.Stq.movimentacao.domain.StatusEstoque;

public record EstoqueAtualItem(
        String sku,
        String nomeProduto,
        String unidadeMedida,
        int saldoAtual,
        int estoqueMinimo,
        StatusEstoque status
) {}
