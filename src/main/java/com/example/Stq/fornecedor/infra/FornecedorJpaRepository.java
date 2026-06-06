package com.example.Stq.fornecedor.infra;

import com.example.Stq.fornecedor.domain.Fornecedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface FornecedorJpaRepository
        extends JpaRepository<Fornecedor, UUID>, JpaSpecificationExecutor<Fornecedor> {
    boolean existsByCnpj(String cnpj);
    boolean existsByCnpjAndIdNot(String cnpj, UUID id);
}
