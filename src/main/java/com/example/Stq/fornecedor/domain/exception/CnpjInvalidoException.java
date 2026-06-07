package com.example.Stq.fornecedor.domain.exception;

public class CnpjInvalidoException extends RuntimeException {
    public CnpjInvalidoException() {
        super("CNPJ inválido.");
    }
}
