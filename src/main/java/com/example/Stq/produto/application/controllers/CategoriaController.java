package com.example.Stq.produto.application.controllers;

import com.example.Stq.produto.application.dto.CategoriaCreateRequest;
import com.example.Stq.produto.application.dto.CategoriaResponse;
import com.example.Stq.produto.application.dto.CategoriaUpdateRequest;
import com.example.Stq.produto.application.services.CategoriaService;
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
@RequestMapping("/api/categorias")
@RequiredArgsConstructor
public class CategoriaController {

    private final CategoriaService categoriaService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoriaResponse> criar(@Valid @RequestBody CategoriaCreateRequest request) {
        CategoriaResponse response = categoriaService.criar(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<CategoriaResponse>> listar(
            @RequestParam(required = false) UUID categoriaPaiId,
            @RequestParam(required = false) Boolean ativo,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(categoriaService.listar(categoriaPaiId, ativo, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CategoriaResponse> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(categoriaService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoriaResponse> atualizar(
            @PathVariable UUID id,
            @Valid @RequestBody CategoriaUpdateRequest request) {
        return ResponseEntity.ok(categoriaService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> desativar(@PathVariable UUID id) {
        categoriaService.desativar(id);
        return ResponseEntity.noContent().build();
    }
}
