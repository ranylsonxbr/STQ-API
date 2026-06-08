package com.example.Stq.movimentacao.application.services;

import com.example.Stq.movimentacao.application.dto.EstoqueResponse;
import com.example.Stq.movimentacao.domain.EstoqueFiltro;
import com.example.Stq.movimentacao.domain.EstoqueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class EstoqueServiceImpl implements EstoqueService {

    private final EstoqueRepository estoqueRepository;

    @Override
    public Page<EstoqueResponse> consultarSaldo(EstoqueFiltro filtro, Pageable pageable) {
        return estoqueRepository.findAll(filtro, pageable).map(EstoqueResponse::de);
    }

    @Override
    public List<EstoqueResponse> listarPorProduto(UUID produtoId) {
        return estoqueRepository.findByProdutoId(produtoId)
                .stream()
                .map(EstoqueResponse::de)
                .toList();
    }
}
