package com.example.Stq.produto.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VariacaoProdutoRepository {
    Optional<VariacaoProduto> findById(UUID id);
    Optional<VariacaoProduto> findByIdAndProdutoId(UUID id, UUID produtoId);
    boolean existsByProdutoIdAndAtributoAndValor(UUID produtoId, String atributo, String valor);
    List<VariacaoProduto> findAtivasByProdutoId(UUID produtoId);
    VariacaoProduto save(VariacaoProduto variacao);
}
