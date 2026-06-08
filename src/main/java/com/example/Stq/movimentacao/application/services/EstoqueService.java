package com.example.Stq.movimentacao.application.services;

import com.example.Stq.movimentacao.application.dto.EstoqueResponse;
import com.example.Stq.movimentacao.domain.EstoqueFiltro;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface EstoqueService {
    Page<EstoqueResponse> consultarSaldo(EstoqueFiltro filtro, Pageable pageable);
    List<EstoqueResponse> listarPorProduto(UUID produtoId);
}
