package com.example.Stq.relatorio.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record PedidoCompraItem(
        UUID id,
        String fornecedorNome,
        String status,
        LocalDate dataEmissao,
        BigDecimal totalPedido
) {}
