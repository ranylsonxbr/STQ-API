package com.example.Stq.movimentacao.domain.exception;

public class SaldoInsuficienteException extends RuntimeException {
    public SaldoInsuficienteException(int saldoAtual, int solicitado) {
        super("Saldo insuficiente: disponível " + saldoAtual + ", solicitado " + solicitado + ".");
    }
}
