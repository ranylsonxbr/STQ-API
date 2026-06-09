package com.example.Stq.produto.infra;

import com.example.Stq.produto.domain.Produto;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ProdutoJpaRepository
        extends JpaRepository<Produto, UUID>, JpaSpecificationExecutor<Produto> {

    @EntityGraph(attributePaths = "variacoes")
    @Query("SELECT p FROM Produto p WHERE p.id = :id")
    Optional<Produto> findByIdComVariacoes(@Param("id") UUID id);

    @EntityGraph(attributePaths = "variacoes")
    @Query("SELECT p FROM Produto p WHERE p.sku = :sku")
    Optional<Produto> findBySkuComVariacoes(@Param("sku") String sku);

    Optional<Produto> findBySku(String sku);

    boolean existsByCategoria_IdAndAtivoTrue(UUID categoriaId);
    boolean existsByNomeIgnoreCase(String nome);
    boolean existsByNomeIgnoreCaseAndIdNot(String nome, UUID id);
}
