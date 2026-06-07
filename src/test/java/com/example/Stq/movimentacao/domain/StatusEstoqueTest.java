package com.example.Stq.movimentacao.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("StatusEstoque.calcular")
class StatusEstoqueTest {

    @Test
    @DisplayName("[ESTQ-S1] saldo zero deve ser ZERADO")
    void saldoZeroDeveSerZerado() {
        assertThat(StatusEstoque.calcular(0, 10)).isEqualTo(StatusEstoque.ZERADO);
    }

    @Test
    @DisplayName("[ESTQ-S1] saldo negativo deve ser ZERADO")
    void saldoNegativoDeveSerZerado() {
        assertThat(StatusEstoque.calcular(-3, 10)).isEqualTo(StatusEstoque.ZERADO);
    }

    @Test
    @DisplayName("[ESTQ-S1] saldo entre zero e mínimo deve ser ABAIXO_MINIMO")
    void saldoAbaixoDoMinimoDeveSerAbaixoMinimo() {
        assertThat(StatusEstoque.calcular(5, 10)).isEqualTo(StatusEstoque.ABAIXO_MINIMO);
    }

    @Test
    @DisplayName("[ESTQ-S1] saldo igual ao mínimo deve ser NORMAL (boundary)")
    void saldoIgualAoMinimoDeveSerNormal() {
        assertThat(StatusEstoque.calcular(10, 10)).isEqualTo(StatusEstoque.NORMAL);
    }

    @Test
    @DisplayName("[ESTQ-S1] saldo acima do mínimo deve ser NORMAL")
    void saldoAcimaDoMinimoDeveSerNormal() {
        assertThat(StatusEstoque.calcular(15, 10)).isEqualTo(StatusEstoque.NORMAL);
    }

    @Test
    @DisplayName("[ESTQ-S1] mínimo zero com saldo positivo deve ser NORMAL")
    void minimoZeroComSaldoPositivoDeveSerNormal() {
        assertThat(StatusEstoque.calcular(1, 0)).isEqualTo(StatusEstoque.NORMAL);
    }
}
