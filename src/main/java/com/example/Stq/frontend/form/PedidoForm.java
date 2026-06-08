package com.example.Stq.frontend.form;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record PedidoForm(
        @NotNull UUID fornecedorId,
        @NotNull LocalDate dataEmissao,
        LocalDate dataPrevisaoEntrega,
        @Size(max = 500) String observacao,
        @NotNull UUID itemProdutoId,
        UUID itemVariacaoId,
        @NotNull @Positive Integer itemQuantidade,
        @NotNull @Positive BigDecimal itemPrecoUnitario
) {}
