package com.example.Stq.produto.domain.exception;

public class CategoriaComFilhasException extends RuntimeException {
    public CategoriaComFilhasException() {
        super("Categoria possui categorias filhas ativas. Desative-as primeiro.");
    }
}
