package com.example.Stq.movimentacao.application.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record AjusteRequest(
        @NotNull UUID produtoId,
        UUID variacaoId,
        @Size(max = 60) String localizacao,
        @NotNull Integer delta,
        @Size(max = 500) String observacao
) {}
