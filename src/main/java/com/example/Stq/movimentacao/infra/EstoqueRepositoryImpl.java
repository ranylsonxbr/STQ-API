package com.example.Stq.movimentacao.infra;

import com.example.Stq.movimentacao.domain.Estoque;
import com.example.Stq.movimentacao.domain.EstoqueFiltro;
import com.example.Stq.movimentacao.domain.EstoqueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class EstoqueRepositoryImpl implements EstoqueRepository {

    private final EstoqueJpaRepository jpa;

    @Override
    public Optional<Estoque> buscarCombinacao(UUID produtoId, UUID variacaoId, String localizacao) {
        return jpa.buscarCombinacao(produtoId, variacaoId, localizacao);
    }

    @Override
    public Optional<Estoque> findById(UUID id) {
        return jpa.findById(id);
    }

    @Override
    public Page<Estoque> findAll(EstoqueFiltro filtro, Pageable pageable) {
        return jpa.findAll(EstoqueSpecs.comFiltro(filtro), pageable);
    }

    @Override
    public Estoque save(Estoque estoque) {
        return jpa.save(estoque);
    }
}
