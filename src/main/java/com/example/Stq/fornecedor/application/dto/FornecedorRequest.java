package com.example.Stq.fornecedor.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FornecedorRequest(
        @NotBlank @Size(min = 3, max = 150) String razaoSocial,
        @NotBlank String cnpj,
        @Email String email,
        String telefone,
        String contato
) {}
