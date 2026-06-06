package com.example.Stq.fornecedor.domain.exception;

public class CnpjDuplicadoException extends RuntimeException {
    public CnpjDuplicadoException() {
        super("Já existe fornecedor com o CNPJ informado.");
    }
}
