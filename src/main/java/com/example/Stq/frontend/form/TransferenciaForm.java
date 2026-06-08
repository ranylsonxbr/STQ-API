package com.example.Stq.frontend.form;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record TransferenciaForm(
        @NotNull UUID produtoId,
        UUID variacaoId,
        @Size(max = 60) String localizacaoOrigem,
        @Size(max = 60) String localizacaoDestino,
        @NotNull @Positive Integer quantidade,
        @Size(max = 500) String observacao
) {}
