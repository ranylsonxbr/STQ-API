package com.example.Stq.produto.application.dto;

import com.example.Stq.produto.domain.Produto;
import com.example.Stq.produto.domain.UnidadeMedida;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ProdutoDetalheResponse(
        UUID id,
        String sku,
        String nome,
        String descricao,
        UUID categoriaId,
        String categoriaNome,
        UnidadeMedida unidadeMedida,
        int estoqueMinimo,
        boolean ativo,
        Instant criadoEm,
        List<VariacaoResponse> variacoes
) {
    public static ProdutoDetalheResponse de(Produto p) {
        List<VariacaoResponse> vars = p.getVariacoes().stream()
                .map(VariacaoResponse::de)
                .toList();
        return new ProdutoDetalheResponse(
                p.getId(), p.getSku(), p.getNome(), p.getDescricao(),
                p.getCategoria().getId(), p.getCategoria().getNome(),
                p.getUnidadeMedida(), p.getEstoqueMinimo(), p.isAtivo(), p.getCriadoEm(),
                vars
        );
    }
}
