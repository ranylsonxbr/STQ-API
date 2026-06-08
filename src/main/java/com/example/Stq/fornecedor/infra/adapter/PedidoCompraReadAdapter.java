package com.example.Stq.fornecedor.infra.adapter;

import com.example.Stq.fornecedor.domain.port.PedidoCompraReadPort;
import com.example.Stq.pedidocompra.domain.PedidoCompraRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("fornecedorPedidoCompraReadAdapter")
@RequiredArgsConstructor
public class PedidoCompraReadAdapter implements PedidoCompraReadPort {

    private final PedidoCompraRepository pedidoCompraRepository;

    @Override
    public boolean existePedidoEmAbertoParaFornecedor(UUID fornecedorId) {
        return pedidoCompraRepository.existePedidoAbertoParaFornecedor(fornecedorId);
    }
}
