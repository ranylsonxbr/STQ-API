package com.example.Stq.fornecedor.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface FornecedorRepository {
    Fornecedor save(Fornecedor fornecedor);
    Optional<Fornecedor> findById(UUID id);
    Page<Fornecedor> findAll(FornecedorFiltro filtro, Pageable pageable);
    boolean existsByCnpj(String cnpj);
    boolean existsByCnpjAndIdNot(String cnpj, UUID id);
}
