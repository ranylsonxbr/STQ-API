package com.example.Stq.frontend.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CategoriaForm(
        @NotBlank @Size(min = 2, max = 60) String nome,
        UUID categoriaPaiId
) {}
