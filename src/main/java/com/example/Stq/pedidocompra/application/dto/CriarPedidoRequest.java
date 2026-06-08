package com.example.Stq.pedidocompra.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record CriarPedidoRequest(
        @NotNull UUID fornecedorId,
        @NotNull LocalDate dataEmissao,
        LocalDate dataPrevisaoEntrega,
        @Size(max = 500) String observacao,
        @NotEmpty @Valid List<ItemRequest> itens
) {}
