package com.example.Stq.pedidocompra.domain.exception;

import com.example.Stq.pedidocompra.domain.StatusPedido;

public class TransicaoStatusInvalidaException extends RuntimeException {
    public TransicaoStatusInvalidaException(StatusPedido atual, String acao) {
        super("Não é possível " + acao + " um pedido no status " + atual + ".");
    }
}
