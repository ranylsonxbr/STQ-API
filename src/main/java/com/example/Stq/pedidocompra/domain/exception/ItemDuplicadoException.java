package com.example.Stq.pedidocompra.domain.exception;

public class ItemDuplicadoException extends RuntimeException {
    public ItemDuplicadoException() {
        super("O produto (e variação) já consta neste pedido. Atualize a quantidade do item existente.");
    }
}
