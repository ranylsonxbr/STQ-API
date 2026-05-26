package com.example.Stq.produto.domain.exception;

public class CategoriaInativaException extends RuntimeException {
    public CategoriaInativaException() {
        super("Categoria está inativa e não pode ser usada.");
    }
}
