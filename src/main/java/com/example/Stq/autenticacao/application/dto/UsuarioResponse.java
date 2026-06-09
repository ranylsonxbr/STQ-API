package com.example.Stq.autenticacao.application.dto;

import com.example.Stq.autenticacao.domain.Usuario;

import java.util.UUID;

public record UsuarioResponse(UUID id, String nome, String email, String perfil, boolean ativo) {
    public static UsuarioResponse de(Usuario u) {
        return new UsuarioResponse(u.getId(), u.getNome(), u.getEmail(), u.getPerfil().name(), u.isAtivo());
    }
}
