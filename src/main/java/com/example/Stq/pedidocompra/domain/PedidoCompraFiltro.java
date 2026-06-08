package com.example.Stq.pedidocompra.domain;

import java.time.LocalDate;
import java.util.UUID;

public record PedidoCompraFiltro(
        UUID fornecedorId,
        StatusPedido status,
        LocalDate de,
        LocalDate ate
) {}
