package com.example.Stq.fornecedor.application.controllers;

import com.example.Stq.autenticacao.infra.UsuarioDetails;
import com.example.Stq.fornecedor.application.dto.FornecedorRequest;
import com.example.Stq.fornecedor.application.dto.FornecedorResponse;
import com.example.Stq.fornecedor.application.services.FornecedorService;
import com.example.Stq.fornecedor.domain.FornecedorFiltro;
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
import java.util.UUID;

@RestController
@RequestMapping("/api/fornecedores")
@RequiredArgsConstructor
public class FornecedorController {

    private final FornecedorService fornecedorService;

    @PostMapping
    @PreAuthorize("hasAnyRole('OPERADOR','ADMIN')")
    public ResponseEntity<FornecedorResponse> criar(
            @Valid @RequestBody FornecedorRequest request,
            @AuthenticationPrincipal UsuarioDetails autenticado) {
        UUID usuarioId = autenticado.getUsuario().getId();
        FornecedorResponse criado = fornecedorService.criar(request, usuarioId);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(criado.id()).toUri();
        return ResponseEntity.created(location).body(criado);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('VISUALIZADOR','OPERADOR','ADMIN')")
    public ResponseEntity<Page<FornecedorResponse>> listar(
            @RequestParam(required = false) String razaoSocial,
            @RequestParam(required = false) String cnpj,
            @RequestParam(required = false, defaultValue = "true") Boolean ativo,
            @PageableDefault(size = 20) Pageable pageable) {
        var filtro = new FornecedorFiltro(razaoSocial, cnpj, ativo);
        return ResponseEntity.ok(fornecedorService.listar(filtro, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('VISUALIZADOR','OPERADOR','ADMIN')")
    public ResponseEntity<FornecedorResponse> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(fornecedorService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('OPERADOR','ADMIN')")
    public ResponseEntity<FornecedorResponse> atualizar(
            @PathVariable UUID id,
            @Valid @RequestBody FornecedorRequest request,
            @AuthenticationPrincipal UsuarioDetails autenticado) {
        var u = autenticado.getUsuario();
        return ResponseEntity.ok(
                fornecedorService.atualizar(id, request, u.getId(), u.getPerfil()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('OPERADOR','ADMIN')")
    public ResponseEntity<Void> desativar(
            @PathVariable UUID id,
            @AuthenticationPrincipal UsuarioDetails autenticado) {
        fornecedorService.desativar(id, autenticado.getUsuario().getId());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/reativar")
    @PreAuthorize("hasAnyRole('OPERADOR','ADMIN')")
    public ResponseEntity<FornecedorResponse> reativar(
            @PathVariable UUID id,
            @AuthenticationPrincipal UsuarioDetails autenticado) {
        return ResponseEntity.ok(
                fornecedorService.reativar(id, autenticado.getUsuario().getId()));
    }
}
