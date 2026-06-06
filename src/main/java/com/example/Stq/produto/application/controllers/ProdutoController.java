package com.example.Stq.produto.application.controllers;

import com.example.Stq.produto.application.dto.*;
import com.example.Stq.produto.application.services.ProdutoService;
import com.example.Stq.produto.application.services.VariacaoProdutoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/produtos")
@RequiredArgsConstructor
public class ProdutoController {

    private final ProdutoService produtoService;
    private final VariacaoProdutoService variacaoProdutoService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','OPERADOR')")
    public ResponseEntity<ProdutoResponse> criar(@Valid @RequestBody ProdutoCreateRequest request) {
        ProdutoResponse response = produtoService.criar(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<ProdutoResponse>> listar(
            @RequestParam(required = false) UUID categoriaId,
            @RequestParam(required = false) Boolean ativo,
            @RequestParam(required = false) String nome,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(produtoService.listar(categoriaId, ativo, nome, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ProdutoDetalheResponse> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(produtoService.buscarPorId(id));
    }

    @GetMapping("/sku/{sku}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ProdutoDetalheResponse> buscarPorSku(@PathVariable String sku) {
        return ResponseEntity.ok(produtoService.buscarPorSku(sku));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','OPERADOR')")
    public ResponseEntity<ProdutoResponse> atualizar(
            @PathVariable UUID id,
            @Valid @RequestBody ProdutoUpdateRequest request) {
        return ResponseEntity.ok(produtoService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> desativar(@PathVariable UUID id) {
        produtoService.desativar(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/variacoes")
    @PreAuthorize("hasAnyRole('ADMIN','OPERADOR')")
    public ResponseEntity<VariacaoResponse> adicionarVariacao(
            @PathVariable UUID id,
            @Valid @RequestBody VariacaoCreateRequest request) {
        VariacaoResponse response = variacaoProdutoService.adicionar(id, request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{variacaoId}").buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(location).body(response);
    }

    @DeleteMapping("/{id}/variacoes/{variacaoId}")
    @PreAuthorize("hasAnyRole('ADMIN','OPERADOR')")
    public ResponseEntity<Void> desativarVariacao(
            @PathVariable UUID id,
            @PathVariable UUID variacaoId) {
        variacaoProdutoService.desativar(id, variacaoId);
        return ResponseEntity.noContent().build();
    }
}
