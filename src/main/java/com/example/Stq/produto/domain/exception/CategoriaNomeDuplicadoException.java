package com.example.Stq.produto.domain.exception;

public class CategoriaNomeDuplicadoException extends RuntimeException {
    public CategoriaNomeDuplicadoException() {
        super("Já existe uma categoria com este nome no mesmo nível.");
    }
}
