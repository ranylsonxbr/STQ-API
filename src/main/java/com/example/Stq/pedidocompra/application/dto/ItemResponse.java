package com.example.Stq.pedidocompra.application.dto;

import com.example.Stq.pedidocompra.domain.ItemPedidoCompra;

import java.math.BigDecimal;
import java.util.UUID;

public record ItemResponse(
        UUID id,
        UUID produtoId,
        String produtoSku,
        UUID variacaoId,
        Integer quantidade,
        BigDecimal precoUnitario,
        BigDecimal subtotal
) {
    public static ItemResponse de(ItemPedidoCompra item) {
        return new ItemResponse(
                item.getId(),
                item.getProduto().getId(),
                item.getProduto().getSku(),
                item.getVariacao() != null ? item.getVariacao().getId() : null,
                item.getQuantidade(),
                item.getPrecoUnitario(),
                item.getSubtotal()
        );
    }
}
