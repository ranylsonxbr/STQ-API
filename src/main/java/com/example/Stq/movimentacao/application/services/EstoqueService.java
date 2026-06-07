package com.example.Stq.movimentacao.application.services;

import com.example.Stq.movimentacao.application.dto.EstoqueResponse;
import com.example.Stq.movimentacao.domain.EstoqueFiltro;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EstoqueService {
    Page<EstoqueResponse> consultarSaldo(EstoqueFiltro filtro, Pageable pageable);
}
