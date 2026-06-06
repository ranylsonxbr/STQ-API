package com.example.Stq.produto.domain;

import java.util.Optional;
import java.util.UUID;

public interface VariacaoProdutoRepository {
    Optional<VariacaoProduto> findById(UUID id);
    Optional<VariacaoProduto> findByIdAndProdutoId(UUID id, UUID produtoId);
    boolean existsByProdutoIdAndAtributoAndValor(UUID produtoId, String atributo, String valor);
    VariacaoProduto save(VariacaoProduto variacao);
}
