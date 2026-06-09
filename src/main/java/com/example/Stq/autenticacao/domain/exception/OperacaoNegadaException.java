package com.example.Stq.autenticacao.domain.exception;

public class OperacaoNegadaException extends RuntimeException {
    public OperacaoNegadaException(String mensagem) {
        super(mensagem);
    }
}
