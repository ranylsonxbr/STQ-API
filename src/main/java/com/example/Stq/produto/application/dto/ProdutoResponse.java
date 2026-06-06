package com.example.Stq.produto.application.dto;

import com.example.Stq.produto.domain.Produto;
import com.example.Stq.produto.domain.UnidadeMedida;

import java.time.Instant;
import java.util.UUID;

public record ProdutoResponse(
        UUID id,
        String sku,
        String nome,
        String descricao,
        UUID categoriaId,
        String categoriaNome,
        UnidadeMedida unidadeMedida,
        int estoqueMinimo,
        boolean ativo,
        Instant criadoEm
) {
    public static ProdutoResponse de(Produto p) {
        return new ProdutoResponse(
                p.getId(), p.getSku(), p.getNome(), p.getDescricao(),
                p.getCategoria().getId(), p.getCategoria().getNome(),
                p.getUnidadeMedida(), p.getEstoqueMinimo(), p.isAtivo(), p.getCriadoEm()
        );
    }
}
