package com.example.Stq.fornecedor.application.dto;

import com.example.Stq.fornecedor.domain.Fornecedor;

import java.time.Instant;
import java.util.UUID;

public record FornecedorResponse(
        UUID id,
        String razaoSocial,
        String cnpj,
        String email,
        String telefone,
        String contato,
        boolean ativo,
        Instant criadoEm,
        Instant atualizadoEm,
        UUID atualizadoPor
) {
    public static FornecedorResponse de(Fornecedor f) {
        return new FornecedorResponse(
                f.getId(), f.getRazaoSocial(), f.getCnpj(), f.getEmail(),
                f.getTelefone(), f.getContato(), f.isAtivo(),
                f.getCriadoEm(), f.getAtualizadoEm(), f.getAtualizadoPor());
    }
}
