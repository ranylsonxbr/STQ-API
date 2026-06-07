package com.example.Stq.fornecedor.domain.port;

import java.util.UUID;

public interface PedidoCompraReadPort {
    boolean existePedidoEmAbertoParaFornecedor(UUID fornecedorId);
}
