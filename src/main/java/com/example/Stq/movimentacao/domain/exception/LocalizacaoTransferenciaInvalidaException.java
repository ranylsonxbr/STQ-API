package com.example.Stq.movimentacao.domain.exception;

public class LocalizacaoTransferenciaInvalidaException extends RuntimeException {
    public LocalizacaoTransferenciaInvalidaException() {
        super("A localização de origem e destino da transferência devem ser diferentes.");
    }
}
