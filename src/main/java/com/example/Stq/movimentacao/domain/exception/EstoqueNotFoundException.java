package com.example.Stq.movimentacao.domain.exception;

import java.util.UUID;

public class EstoqueNotFoundException extends RuntimeException {
    public EstoqueNotFoundException(UUID produtoId) {
        super("Estoque não encontrado para o produto: " + produtoId);
    }
}
