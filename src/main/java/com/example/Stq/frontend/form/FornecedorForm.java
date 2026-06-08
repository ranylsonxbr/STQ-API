package com.example.Stq.frontend.form;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FornecedorForm(
        @NotBlank @Size(min = 3, max = 150) String razaoSocial,
        @NotBlank String cnpj,
        @Email String email,
        String telefone,
        String contato
) {}
