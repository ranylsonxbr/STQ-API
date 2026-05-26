package com.example.Stq.produto.domain;

import java.util.UUID;

public record ProdutoFiltro(UUID categoriaId, Boolean ativo, String nome) {}
