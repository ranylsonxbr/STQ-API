package com.example.Stq.movimentacao.application.controllers;

import com.example.Stq.autenticacao.infra.UsuarioDetails;
import com.example.Stq.movimentacao.application.dto.AjusteRequest;
import com.example.Stq.movimentacao.application.dto.EntradaRequest;
import com.example.Stq.movimentacao.application.dto.MovimentacaoResponse;
import com.example.Stq.movimentacao.application.dto.SaidaRequest;
import com.example.Stq.movimentacao.application.dto.TransferenciaRequest;
import com.example.Stq.movimentacao.application.services.MovimentacaoService;
import com.example.Stq.movimentacao.domain.MovimentacaoFiltro;
import com.example.Stq.movimentacao.domain.TipoMovimentacao;
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
import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/movimentacoes")
@RequiredArgsConstructor
public class MovimentacaoController {

    private final MovimentacaoService movimentacaoService;

    @PostMapping("/entrada")
    @PreAuthorize("hasAnyRole('OPERADOR','ADMIN')")
    public ResponseEntity<MovimentacaoResponse> registrarEntrada(
            @Valid @RequestBody EntradaRequest request,
            @AuthenticationPrincipal UsuarioDetails autenticado) {
        var criada = movimentacaoService.registrarEntrada(request, autenticado.getUsuario().getId());
        return created(criada);
    }

    @PostMapping("/saida")
    @PreAuthorize("hasAnyRole('OPERADOR','ADMIN')")
    public ResponseEntity<MovimentacaoResponse> registrarSaida(
            @Valid @RequestBody SaidaRequest request,
            @AuthenticationPrincipal UsuarioDetails autenticado) {
        var criada = movimentacaoService.registrarSaida(request, autenticado.getUsuario().getId());
        return created(criada);
    }

    @PostMapping("/transferencia")
    @PreAuthorize("hasAnyRole('OPERADOR','ADMIN')")
    public ResponseEntity<MovimentacaoResponse> registrarTransferencia(
            @Valid @RequestBody TransferenciaRequest request,
            @AuthenticationPrincipal UsuarioDetails autenticado) {
        var criada = movimentacaoService.registrarTransferencia(request, autenticado.getUsuario().getId());
        return created(criada);
    }

    @PostMapping("/ajuste")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MovimentacaoResponse> registrarAjuste(
            @Valid @RequestBody AjusteRequest request,
            @AuthenticationPrincipal UsuarioDetails autenticado) {
        var criada = movimentacaoService.registrarAjuste(request, autenticado.getUsuario().getId());
        return created(criada);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('VISUALIZADOR','OPERADOR','ADMIN')")
    public ResponseEntity<Page<MovimentacaoResponse>> listar(
            @RequestParam(required = false) UUID produtoId,
            @RequestParam(required = false) TipoMovimentacao tipo,
            @RequestParam(required = false) Instant de,
            @RequestParam(required = false) Instant ate,
            @PageableDefault(size = 20) Pageable pageable) {
        var filtro = new MovimentacaoFiltro(produtoId, tipo, de, ate);
        return ResponseEntity.ok(movimentacaoService.listar(filtro, pageable));
    }

    private ResponseEntity<MovimentacaoResponse> created(MovimentacaoResponse criada) {
        URI location = ServletUriComponentsBuilder.fromCurrentRequestUri()
                .replacePath("/api/movimentacoes/{id}")
                .buildAndExpand(criada.id()).toUri();
        return ResponseEntity.created(location).body(criada);
    }
}
