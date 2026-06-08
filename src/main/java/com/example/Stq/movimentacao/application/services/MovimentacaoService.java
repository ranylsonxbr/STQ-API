package com.example.Stq.movimentacao.application.services;

import com.example.Stq.movimentacao.application.dto.AjusteRequest;
import com.example.Stq.movimentacao.application.dto.EntradaPedidoComando;
import com.example.Stq.movimentacao.application.dto.EntradaRequest;
import com.example.Stq.movimentacao.application.dto.MovimentacaoResponse;
import com.example.Stq.movimentacao.application.dto.SaidaRequest;
import com.example.Stq.movimentacao.application.dto.TransferenciaRequest;
import com.example.Stq.movimentacao.domain.MovimentacaoFiltro;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface MovimentacaoService {
    MovimentacaoResponse registrarEntrada(EntradaRequest request, UUID usuarioId);
    MovimentacaoResponse registrarEntradaPorPedido(EntradaPedidoComando comando, UUID usuarioId);
    MovimentacaoResponse registrarSaida(SaidaRequest request, UUID usuarioId);
    MovimentacaoResponse registrarTransferencia(TransferenciaRequest request, UUID usuarioId);
    MovimentacaoResponse registrarAjuste(AjusteRequest request, UUID usuarioId);
    Page<MovimentacaoResponse> listar(MovimentacaoFiltro filtro, Pageable pageable);
}
