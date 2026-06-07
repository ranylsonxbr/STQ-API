package com.example.Stq.movimentacao.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface MovimentacaoRepository {
    Movimentacao save(Movimentacao movimentacao);
    Optional<Movimentacao> findById(UUID id);
    Page<Movimentacao> findAll(MovimentacaoFiltro filtro, Pageable pageable);
}
