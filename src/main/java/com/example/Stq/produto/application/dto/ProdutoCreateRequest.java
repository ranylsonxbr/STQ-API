package com.example.Stq.produto.application.dto;

import com.example.Stq.produto.domain.UnidadeMedida;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record ProdutoCreateRequest(
        @NotBlank @Size(min = 3, max = 120) String nome,
        @Size(max = 1000) String descricao,
        @NotNull UUID categoriaId,
        @NotNull UnidadeMedida unidadeMedida,
        @PositiveOrZero Integer estoqueMinimo
) {}
