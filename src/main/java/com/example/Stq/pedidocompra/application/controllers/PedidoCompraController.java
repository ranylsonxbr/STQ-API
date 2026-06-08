package com.example.Stq.pedidocompra.application.controllers;

import com.example.Stq.autenticacao.infra.UsuarioDetails;
import com.example.Stq.pedidocompra.application.dto.CriarPedidoRequest;
import com.example.Stq.pedidocompra.application.dto.ItemRequest;
import com.example.Stq.pedidocompra.application.dto.PedidoCompraResponse;
import com.example.Stq.pedidocompra.application.services.PedidoCompraService;
import com.example.Stq.pedidocompra.domain.PedidoCompraFiltro;
import com.example.Stq.pedidocompra.domain.StatusPedido;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/pedidos-compra")
@RequiredArgsConstructor
public class PedidoCompraController {

    private final PedidoCompraService pedidoCompraService;

    @PostMapping
    @PreAuthorize("hasAnyRole('OPERADOR','ADMIN')")
    public ResponseEntity<PedidoCompraResponse> criar(
            @Valid @RequestBody CriarPedidoRequest request,
            @AuthenticationPrincipal UsuarioDetails autenticado) {
        var criado = pedidoCompraService.criar(request, autenticado.getUsuario().getId());
        URI location = ServletUriComponentsBuilder.fromCurrentRequestUri()
                .path("/{id}").buildAndExpand(criado.id()).toUri();
        return ResponseEntity.created(location).body(criado);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('VISUALIZADOR','OPERADOR','ADMIN')")
    public ResponseEntity<PedidoCompraResponse> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(pedidoCompraService.buscarPorId(id));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('VISUALIZADOR','OPERADOR','ADMIN')")
    public ResponseEntity<Page<PedidoCompraResponse>> listar(
            @RequestParam(required = false) UUID fornecedorId,
            @RequestParam(required = false) StatusPedido status,
            @RequestParam(required = false) LocalDate de,
            @RequestParam(required = false) LocalDate ate,
            @PageableDefault(size = 20) Pageable pageable) {
        var filtro = new PedidoCompraFiltro(fornecedorId, status, de, ate);
        return ResponseEntity.ok(pedidoCompraService.listar(filtro, pageable));
    }

    @PostMapping("/{id}/itens")
    @PreAuthorize("hasAnyRole('OPERADOR','ADMIN')")
    public ResponseEntity<PedidoCompraResponse> adicionarItem(
            @PathVariable UUID id, @Valid @RequestBody ItemRequest request) {
        return ResponseEntity.ok(pedidoCompraService.adicionarItem(id, request));
    }

    @DeleteMapping("/{id}/itens/{itemId}")
    @PreAuthorize("hasAnyRole('OPERADOR','ADMIN')")
    public ResponseEntity<PedidoCompraResponse> removerItem(
            @PathVariable UUID id, @PathVariable UUID itemId) {
        return ResponseEntity.ok(pedidoCompraService.removerItem(id, itemId));
    }

    @PatchMapping("/{id}/enviar")
    @PreAuthorize("hasAnyRole('OPERADOR','ADMIN')")
    public ResponseEntity<PedidoCompraResponse> enviar(@PathVariable UUID id) {
        return ResponseEntity.ok(pedidoCompraService.enviar(id));
    }

    @PatchMapping("/{id}/aprovar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PedidoCompraResponse> aprovar(@PathVariable UUID id) {
        return ResponseEntity.ok(pedidoCompraService.aprovar(id));
    }

    @PatchMapping("/{id}/receber")
    @PreAuthorize("hasAnyRole('OPERADOR','ADMIN')")
    public ResponseEntity<PedidoCompraResponse> receber(
            @PathVariable UUID id, @AuthenticationPrincipal UsuarioDetails autenticado) {
        return ResponseEntity.ok(pedidoCompraService.receber(id, autenticado.getUsuario().getId()));
    }

    @PatchMapping("/{id}/cancelar")
    @PreAuthorize("hasAnyRole('OPERADOR','ADMIN')")
    public ResponseEntity<PedidoCompraResponse> cancelar(@PathVariable UUID id) {
        return ResponseEntity.ok(pedidoCompraService.cancelar(id));
    }
}
