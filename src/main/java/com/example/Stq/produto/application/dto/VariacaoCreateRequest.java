package com.example.Stq.produto.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record VariacaoCreateRequest(
        @NotBlank @Size(max = 60) String atributo,
        @NotBlank @Size(max = 120) String valor
) {}
