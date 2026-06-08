package com.example.Stq.produto.infra;

import com.example.Stq.produto.domain.VariacaoProduto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VariacaoProdutoJpaRepository extends JpaRepository<VariacaoProduto, UUID> {

    Optional<VariacaoProduto> findByIdAndProduto_Id(UUID id, UUID produtoId);

    boolean existsByProduto_IdAndAtributoAndValor(UUID produtoId, String atributo, String valor);

    List<VariacaoProduto> findByProduto_IdAndAtivoTrue(UUID produtoId);
}
