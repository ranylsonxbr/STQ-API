package com.example.Stq.produto.domain.exception;

import java.util.UUID;

public class CategoriaNotFoundException extends RuntimeException {
    public CategoriaNotFoundException(UUID id) {
        super("Categoria não encontrada: " + id);
    }
}
