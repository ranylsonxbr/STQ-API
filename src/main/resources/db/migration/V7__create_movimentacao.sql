CREATE TABLE movimentacao (
    id            UUID         NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    produto_id    UUID         NOT NULL REFERENCES produto(id),
    variacao_id   UUID         NULL     REFERENCES variacao_produto(id),
    tipo          VARCHAR(15)  NOT NULL CHECK (tipo IN ('ENTRADA','SAIDA','TRANSFERENCIA','AJUSTE')),
    origem        VARCHAR(20)  NOT NULL CHECK (origem IN ('MANUAL','PEDIDO_COMPRA','AJUSTE_INVENTARIO')),
    quantidade    INTEGER      NOT NULL CHECK (quantidade > 0),
    saldo_antes   INTEGER      NOT NULL,
    saldo_depois  INTEGER      NOT NULL,
    observacao    VARCHAR(500) NULL,
    realizado_por UUID         NOT NULL REFERENCES usuario(id),
    realizado_em  TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_movimentacao_produto_data ON movimentacao(produto_id, realizado_em DESC);
CREATE INDEX idx_movimentacao_tipo         ON movimentacao(tipo);
CREATE INDEX idx_movimentacao_data         ON movimentacao(realizado_em DESC);
