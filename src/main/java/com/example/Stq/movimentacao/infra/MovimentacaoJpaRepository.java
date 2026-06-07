package com.example.Stq.movimentacao.infra;

import com.example.Stq.movimentacao.domain.Movimentacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface MovimentacaoJpaRepository
        extends JpaRepository<Movimentacao, UUID>, JpaSpecificationExecutor<Movimentacao> {
}
