package com.example.Stq.pedidocompra.application.dto;

import com.example.Stq.pedidocompra.domain.PedidoCompra;
import com.example.Stq.pedidocompra.domain.StatusPedido;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record PedidoCompraResponse(
        UUID id,
        UUID fornecedorId,
        String fornecedorNome,
        StatusPedido status,
        LocalDate dataEmissao,
        LocalDate dataPrevisaoEntrega,
        String observacao,
        BigDecimal totalPedido,
        List<ItemResponse> itens,
        Instant criadoEm
) {
    public static PedidoCompraResponse de(PedidoCompra pedido) {
        return new PedidoCompraResponse(
                pedido.getId(),
                pedido.getFornecedor().getId(),
                pedido.getFornecedor().getRazaoSocial(),
                pedido.getStatus(),
                pedido.getDataEmissao(),
                pedido.getDataPrevisaoEntrega(),
                pedido.getObservacao(),
                pedido.getTotalPedido(),
                pedido.getItens().stream().map(ItemResponse::de).toList(),
                pedido.getCriadoEm()
        );
    }
}
