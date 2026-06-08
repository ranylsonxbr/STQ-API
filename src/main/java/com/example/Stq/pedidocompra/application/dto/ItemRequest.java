package com.example.Stq.pedidocompra.application.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record ItemRequest(
        @NotNull UUID produtoId,
        UUID variacaoId,
        @NotNull @Positive Integer quantidade,
        @NotNull @Positive BigDecimal precoUnitario
) {}
