package com.example.Stq.produto.infra;

import com.example.Stq.produto.domain.VariacaoProduto;
import com.example.Stq.produto.domain.VariacaoProdutoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class VariacaoProdutoRepositoryImpl implements VariacaoProdutoRepository {

    private final VariacaoProdutoJpaRepository jpa;

    @Override
    public Optional<VariacaoProduto> findById(UUID id) {
        return jpa.findById(id);
    }

    @Override
    public Optional<VariacaoProduto> findByIdAndProdutoId(UUID id, UUID produtoId) {
        return jpa.findByIdAndProduto_Id(id, produtoId);
    }

    @Override
    public boolean existsByProdutoIdAndAtributoAndValor(UUID produtoId, String atributo, String valor) {
        return jpa.existsByProduto_IdAndAtributoAndValor(produtoId, atributo, valor);
    }

    @Override
    public List<VariacaoProduto> findAtivasByProdutoId(UUID produtoId) {
        return jpa.findByProduto_IdAndAtivoTrue(produtoId);
    }

    @Override
    public VariacaoProduto save(VariacaoProduto variacao) {
        return jpa.save(variacao);
    }
}
