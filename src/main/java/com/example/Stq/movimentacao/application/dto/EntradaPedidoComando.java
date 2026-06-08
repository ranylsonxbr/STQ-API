package com.example.Stq.movimentacao.application.dto;

import java.util.UUID;

public record EntradaPedidoComando(
        UUID produtoId,
        UUID variacaoId,
        Integer quantidade,
        String observacao
) {}
