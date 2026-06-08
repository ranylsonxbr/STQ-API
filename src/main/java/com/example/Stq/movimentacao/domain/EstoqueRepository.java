package com.example.Stq.movimentacao.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EstoqueRepository {
    Optional<Estoque> buscarCombinacao(UUID produtoId, UUID variacaoId, String localizacao);
    Optional<Estoque> findById(UUID id);
    Page<Estoque> findAll(EstoqueFiltro filtro, Pageable pageable);
    List<Estoque> findByProdutoId(UUID produtoId);
    Estoque save(Estoque estoque);
}
