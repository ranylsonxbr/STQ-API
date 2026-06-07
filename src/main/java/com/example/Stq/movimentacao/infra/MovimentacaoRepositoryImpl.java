package com.example.Stq.movimentacao.infra;

import com.example.Stq.movimentacao.domain.Movimentacao;
import com.example.Stq.movimentacao.domain.MovimentacaoFiltro;
import com.example.Stq.movimentacao.domain.MovimentacaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class MovimentacaoRepositoryImpl implements MovimentacaoRepository {

    private final MovimentacaoJpaRepository jpa;

    @Override
    public Movimentacao save(Movimentacao movimentacao) {
        return jpa.save(movimentacao);
    }

    @Override
    public Optional<Movimentacao> findById(UUID id) {
        return jpa.findById(id);
    }

    @Override
    public Page<Movimentacao> findAll(MovimentacaoFiltro filtro, Pageable pageable) {
        Pageable ordenado = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "realizadoEm"));
        return jpa.findAll(MovimentacaoSpecs.comFiltro(filtro), ordenado);
    }
}
