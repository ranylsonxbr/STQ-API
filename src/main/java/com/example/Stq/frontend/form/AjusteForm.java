package com.example.Stq.frontend.form;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record AjusteForm(
        @NotNull UUID produtoId,
        UUID variacaoId,
        @Size(max = 60) String localizacao,
        @NotNull Integer delta,
        @Size(max = 500) String observacao
) {}
