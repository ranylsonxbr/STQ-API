package com.example.Stq.produto.application.dto;

import com.example.Stq.produto.domain.Categoria;

import java.util.UUID;

public record CategoriaResponse(
        UUID id,
        String nome,
        UUID categoriaPaiId,
        boolean ativo
) {
    public static CategoriaResponse de(Categoria c) {
        return new CategoriaResponse(
                c.getId(),
                c.getNome(),
                c.getCategoriaPai() != null ? c.getCategoriaPai().getId() : null,
                c.isAtivo()
        );
    }
}
