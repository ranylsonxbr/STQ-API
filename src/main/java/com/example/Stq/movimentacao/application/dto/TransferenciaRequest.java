package com.example.Stq.movimentacao.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record TransferenciaRequest(
        @NotNull UUID produtoId,
        UUID variacaoId,
        @NotBlank @Size(max = 60) String localizacaoOrigem,
        @NotBlank @Size(max = 60) String localizacaoDestino,
        @NotNull @Positive Integer quantidade,
        @Size(max = 500) String observacao
) {}
