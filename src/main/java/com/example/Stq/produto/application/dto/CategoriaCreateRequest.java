package com.example.Stq.produto.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CategoriaCreateRequest(
        @NotBlank @Size(min = 2, max = 60) String nome,
        UUID categoriaPaiId
) {}
