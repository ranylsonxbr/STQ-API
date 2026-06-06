package com.example.Stq.produto.domain.exception;

public class VariacaoDuplicadaException extends RuntimeException {
    public VariacaoDuplicadaException(String atributo, String valor) {
        super("Variação '" + atributo + "=" + valor + "' já existe para este produto.");
    }
}
