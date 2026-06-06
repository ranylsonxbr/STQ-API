package com.example.Stq.fornecedor.infra;

import com.example.Stq.fornecedor.domain.Fornecedor;
import com.example.Stq.fornecedor.domain.FornecedorFiltro;
import com.example.Stq.fornecedor.domain.FornecedorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class FornecedorRepositoryImpl implements FornecedorRepository {

    private final FornecedorJpaRepository jpa;

    @Override
    public Fornecedor save(Fornecedor fornecedor) {
        return jpa.save(fornecedor);
    }

    @Override
    public Optional<Fornecedor> findById(UUID id) {
        return jpa.findById(id);
    }

    @Override
    public Page<Fornecedor> findAll(FornecedorFiltro filtro, Pageable pageable) {
        return jpa.findAll(FornecedorSpecs.comFiltro(filtro), pageable);
    }

    @Override
    public boolean existsByCnpj(String cnpj) {
        return jpa.existsByCnpj(cnpj);
    }

    @Override
    public boolean existsByCnpjAndIdNot(String cnpj, UUID id) {
        return jpa.existsByCnpjAndIdNot(cnpj, id);
    }
}
