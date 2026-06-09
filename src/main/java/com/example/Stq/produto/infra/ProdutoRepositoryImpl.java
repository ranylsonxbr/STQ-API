package com.example.Stq.produto.infra;

import com.example.Stq.produto.domain.Produto;
import com.example.Stq.produto.domain.ProdutoFiltro;
import com.example.Stq.produto.domain.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ProdutoRepositoryImpl implements ProdutoRepository {

    private final ProdutoJpaRepository jpa;

    @Override
    public Optional<Produto> findById(UUID id) {
        return jpa.findById(id);
    }

    @Override
    public Optional<Produto> findByIdComVariacoes(UUID id) {
        return jpa.findByIdComVariacoes(id);
    }

    @Override
    public Optional<Produto> findBySkuComVariacoes(String sku) {
        return jpa.findBySkuComVariacoes(sku);
    }

    @Override
    public Optional<Produto> findBySku(String sku) {
        return jpa.findBySku(sku);
    }

    @Override
    public boolean existsByCategoria_IdAndAtivoTrue(UUID categoriaId) {
        return jpa.existsByCategoria_IdAndAtivoTrue(categoriaId);
    }

    @Override
    public boolean existsByNomeIgnoreCase(String nome) {
        return jpa.existsByNomeIgnoreCase(nome);
    }

    @Override
    public boolean existsByNomeIgnoreCaseAndIdNot(String nome, UUID id) {
        return jpa.existsByNomeIgnoreCaseAndIdNot(nome, id);
    }

    @Override
    public Page<Produto> findAll(ProdutoFiltro filtro, Pageable pageable) {
        Specification<Produto> spec = (root, query, cb) -> cb.conjunction();

        if (filtro.categoriaId() != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("categoria").get("id"), filtro.categoriaId()));
        }
        if (filtro.ativo() != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("ativo"), filtro.ativo()));
        }
        if (filtro.nome() != null && !filtro.nome().isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("nome")), "%" + filtro.nome().toLowerCase() + "%"));
        }

        return jpa.findAll(spec, pageable);
    }

    @Override
    public Produto save(Produto produto) {
        return jpa.save(produto);
    }
}
