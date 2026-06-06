package com.example.Stq.produto.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface ProdutoRepository {
    Optional<Produto> findById(UUID id);
    Optional<Produto> findByIdComVariacoes(UUID id);
    Optional<Produto> findBySkuComVariacoes(String sku);
    Optional<Produto> findBySku(String sku);
    boolean existsByCategoria_IdAndAtivoTrue(UUID categoriaId);
    Page<Produto> findAll(ProdutoFiltro filtro, Pageable pageable);
    Produto save(Produto produto);
}
