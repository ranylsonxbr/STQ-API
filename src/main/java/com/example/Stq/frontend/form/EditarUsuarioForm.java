package com.example.Stq.frontend.form;

import com.example.Stq.autenticacao.domain.Perfil;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record EditarUsuarioForm(
        @NotBlank @Size(min = 2, max = 100) String nome,
        @NotBlank @Email String email,
        @NotNull Perfil perfil,
        @Size(min = 8) String novaSenha,
        String confirmarNovaSenha
) {}
