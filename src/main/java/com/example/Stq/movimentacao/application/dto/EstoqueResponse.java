package com.example.Stq.movimentacao.application.dto;

import com.example.Stq.movimentacao.domain.Estoque;
import com.example.Stq.movimentacao.domain.StatusEstoque;

import java.time.Instant;
import java.util.UUID;

public record EstoqueResponse(
        UUID id,
        UUID produtoId,
        String produtoSku,
        UUID variacaoId,
        String localizacao,
        Integer saldoAtual,
        Integer estoqueMinimo,
        StatusEstoque status,
        Instant atualizadoEm
) {
    public static EstoqueResponse de(Estoque e) {
        int minimo = e.getProduto().getEstoqueMinimo() != null ? e.getProduto().getEstoqueMinimo() : 0;
        return new EstoqueResponse(
                e.getId(),
                e.getProduto().getId(),
                e.getProduto().getSku(),
                e.getVariacao() != null ? e.getVariacao().getId() : null,
                e.getLocalizacao(),
                e.getSaldoAtual(),
                minimo,
                StatusEstoque.calcular(e.getSaldoAtual(), minimo),
                e.getAtualizadoEm()
        );
    }
}
