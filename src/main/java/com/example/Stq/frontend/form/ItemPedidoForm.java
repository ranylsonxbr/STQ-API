package com.example.Stq.frontend.form;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record ItemPedidoForm(
        @NotNull UUID produtoId,
        UUID variacaoId,
        @NotNull @Positive Integer quantidade,
        @NotNull @Positive BigDecimal precoUnitario
) {}
