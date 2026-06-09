package com.example.Stq.produto.domain.exception;

public class ProdutoNomeDuplicadoException extends RuntimeException {
    public ProdutoNomeDuplicadoException() {
        super("Já existe um produto cadastrado com este nome.");
    }
}
