package com.example.Stq.movimentacao.domain;

public enum StatusEstoque {
    ZERADO,
    ABAIXO_MINIMO,
    NORMAL;

    public static StatusEstoque calcular(int saldoAtual, int estoqueMinimo) {
        if (saldoAtual <= 0) return ZERADO;
        if (saldoAtual < estoqueMinimo) return ABAIXO_MINIMO;
        return NORMAL;
    }
}
