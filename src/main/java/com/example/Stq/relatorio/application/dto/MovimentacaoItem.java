package com.example.Stq.relatorio.application.dto;

import java.time.Instant;

public record MovimentacaoItem(
        String produtoSku,
        String produtoNome,
        String tipo,
        String origem,
        int quantidade,
        Instant realizadoEm
) {}
