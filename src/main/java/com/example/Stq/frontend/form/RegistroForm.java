package com.example.Stq.frontend.form;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegistroForm(
        @NotBlank @Size(min = 2, max = 100) String nome,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8) String senha,
        @NotBlank String confirmarSenha
) {}
