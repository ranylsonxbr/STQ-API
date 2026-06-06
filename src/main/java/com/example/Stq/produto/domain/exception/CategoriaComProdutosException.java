package com.example.Stq.produto.domain.exception;

public class CategoriaComProdutosException extends RuntimeException {
    public CategoriaComProdutosException() {
        super("Categoria possui produtos ativos e não pode ser desativada.");
    }
}
