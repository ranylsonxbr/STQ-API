package com.example.Stq.pedidocompra.application.services;

import com.example.Stq.pedidocompra.application.dto.CriarPedidoRequest;
import com.example.Stq.pedidocompra.application.dto.ItemRequest;
import com.example.Stq.pedidocompra.application.dto.PedidoCompraResponse;
import com.example.Stq.pedidocompra.domain.PedidoCompraFiltro;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface PedidoCompraService {
    PedidoCompraResponse criar(CriarPedidoRequest request, UUID usuarioId);
    PedidoCompraResponse buscarPorId(UUID id);
    Page<PedidoCompraResponse> listar(PedidoCompraFiltro filtro, Pageable pageable);
    PedidoCompraResponse adicionarItem(UUID pedidoId, ItemRequest request);
    PedidoCompraResponse removerItem(UUID pedidoId, UUID itemId);
    PedidoCompraResponse enviar(UUID pedidoId);
    PedidoCompraResponse aprovar(UUID pedidoId);
    PedidoCompraResponse receber(UUID pedidoId, UUID usuarioId);
    PedidoCompraResponse cancelar(UUID pedidoId);
}
