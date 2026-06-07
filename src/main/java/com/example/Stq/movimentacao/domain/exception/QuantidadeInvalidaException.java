package com.example.Stq.movimentacao.domain.exception;

public class QuantidadeInvalidaException extends RuntimeException {
    public QuantidadeInvalidaException(String mensagem) {
        super(mensagem);
    }
}
