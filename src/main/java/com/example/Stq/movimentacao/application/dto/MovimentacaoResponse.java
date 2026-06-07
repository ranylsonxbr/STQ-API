package com.example.Stq.movimentacao.application.dto;

import com.example.Stq.movimentacao.domain.Movimentacao;
import com.example.Stq.movimentacao.domain.OrigemMovimentacao;
import com.example.Stq.movimentacao.domain.TipoMovimentacao;

import java.time.Instant;
import java.util.UUID;

public record MovimentacaoResponse(
        UUID id,
        UUID produtoId,
        String produtoSku,
        UUID variacaoId,
        TipoMovimentacao tipo,
        OrigemMovimentacao origem,
        Integer quantidade,
        Integer saldoAntes,
        Integer saldoDepois,
        String observacao,
        UUID realizadoPor,
        Instant realizadoEm
) {
    public static MovimentacaoResponse de(Movimentacao m) {
        return new MovimentacaoResponse(
                m.getId(),
                m.getProduto().getId(),
                m.getProduto().getSku(),
                m.getVariacao() != null ? m.getVariacao().getId() : null,
                m.getTipo(),
                m.getOrigem(),
                m.getQuantidade(),
                m.getSaldoAntes(),
                m.getSaldoDepois(),
                m.getObservacao(),
                m.getRealizadoPor().getId(),
                m.getRealizadoEm()
        );
    }
}
