package com.example.Stq.produto.domain.exception;

import java.util.UUID;

public class VariacaoNotFoundException extends RuntimeException {
    public VariacaoNotFoundException(UUID id) {
        super("Variação não encontrada: " + id);
    }
}
