package com.example.Stq.fornecedor.infra.adapter;

import com.example.Stq.fornecedor.domain.port.PedidoCompraReadPort;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("fornecedorPedidoCompraReadAdapter")
public class PedidoCompraReadAdapter implements PedidoCompraReadPort {
    @Override
    public boolean existePedidoEmAbertoParaFornecedor(UUID fornecedorId) {
        return false;
    }
}
