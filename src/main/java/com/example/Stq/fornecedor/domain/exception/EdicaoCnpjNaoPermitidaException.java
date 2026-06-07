package com.example.Stq.fornecedor.domain.exception;

public class EdicaoCnpjNaoPermitidaException extends RuntimeException {
    public EdicaoCnpjNaoPermitidaException() {
        super("Edição de CNPJ permitida apenas para ADMIN.");
    }
}
