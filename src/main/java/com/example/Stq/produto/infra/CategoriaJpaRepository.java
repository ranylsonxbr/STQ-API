package com.example.Stq.produto.infra;

import com.example.Stq.produto.domain.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface CategoriaJpaRepository
        extends JpaRepository<Categoria, UUID>, JpaSpecificationExecutor<Categoria> {

    boolean existsByNomeAndCategoriaPai_Id(String nome, UUID categoriaPaiId);
    boolean existsByNomeAndCategoriaPaiIsNull(String nome);
    boolean existsByCategoriaPai_Id(UUID categoriaPaiId);
    boolean existsByCategoriaPai_IdAndAtivoTrue(UUID categoriaPaiId);
}
