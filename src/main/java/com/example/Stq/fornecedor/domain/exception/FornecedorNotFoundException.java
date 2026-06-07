package com.example.Stq.fornecedor.domain.exception;

import java.util.UUID;

public class FornecedorNotFoundException extends RuntimeException {
    public FornecedorNotFoundException(UUID id) {
        super("Fornecedor não encontrado: " + id);
    }
}
