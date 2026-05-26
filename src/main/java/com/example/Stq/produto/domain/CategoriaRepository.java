package com.example.Stq.produto.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface CategoriaRepository {
    Optional<Categoria> findById(UUID id);
    boolean existsByNomeAndCategoriaPaiId(String nome, UUID categoriaPaiId);
    boolean existsByNomeAndCategoriaPaiIsNull(String nome);
    boolean existsByCategoriaPaiId(UUID categoriaPaiId);
    boolean existsByCategoriaPaiIdAndAtivoTrue(UUID categoriaPaiId);
    Page<Categoria> findAll(UUID categoriaPaiId, Boolean ativo, Pageable pageable);
    Categoria save(Categoria categoria);
}
