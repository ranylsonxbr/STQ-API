package com.example.Stq.frontend.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record ProdutoForm(
        @NotBlank @Size(min = 3, max = 120) String nome,
        @Size(max = 1000) String descricao,
        @NotNull UUID categoriaId,
        @NotBlank String unidadeMedida,
        @PositiveOrZero Integer estoqueMinimo
) {}
