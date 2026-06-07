package com.example.Stq.movimentacao.domain;

import java.time.Instant;
import java.util.UUID;

public record MovimentacaoFiltro(
        UUID produtoId,
        TipoMovimentacao tipo,
        Instant de,
        Instant ate
) {}
