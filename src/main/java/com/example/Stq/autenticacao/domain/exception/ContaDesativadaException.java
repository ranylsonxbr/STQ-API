package com.example.Stq.autenticacao.domain.exception;

public class ContaDesativadaException extends RuntimeException {
    public ContaDesativadaException() {
        super("Conta desativada.");
    }
}
