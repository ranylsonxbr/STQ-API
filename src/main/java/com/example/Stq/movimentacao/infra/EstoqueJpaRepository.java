package com.example.Stq.movimentacao.infra;

import com.example.Stq.movimentacao.domain.Estoque;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface EstoqueJpaRepository
        extends JpaRepository<Estoque, UUID>, JpaSpecificationExecutor<Estoque> {

    @EntityGraph(attributePaths = {"produto", "variacao"})
    @Query("""
           SELECT e FROM Estoque e
           WHERE e.produto.id = :produtoId
             AND ((:variacaoId IS NULL AND e.variacao IS NULL) OR e.variacao.id = :variacaoId)
             AND ((:localizacao IS NULL AND e.localizacao IS NULL) OR e.localizacao = :localizacao)
           """)
    Optional<Estoque> buscarCombinacao(@Param("produtoId") UUID produtoId,
                                       @Param("variacaoId") UUID variacaoId,
                                       @Param("localizacao") String localizacao);
}
