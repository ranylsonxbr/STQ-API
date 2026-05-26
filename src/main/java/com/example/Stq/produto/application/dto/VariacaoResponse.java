package com.example.Stq.produto.application.dto;

import com.example.Stq.produto.domain.VariacaoProduto;

import java.util.UUID;

public record VariacaoResponse(
        UUID id,
        String atributo,
        String valor,
        String skuVariacao,
        boolean ativo
) {
    public static VariacaoResponse de(VariacaoProduto v) {
        return new VariacaoResponse(v.getId(), v.getAtributo(), v.getValor(), v.getSkuVariacao(), v.isAtivo());
    }
}
