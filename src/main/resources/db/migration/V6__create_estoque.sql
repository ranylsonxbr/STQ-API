CREATE TABLE estoque (
    id            UUID        NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    produto_id    UUID        NOT NULL REFERENCES produto(id),
    variacao_id   UUID        NULL     REFERENCES variacao_produto(id),
    localizacao   VARCHAR(60) NULL,
    saldo_atual   INTEGER     NOT NULL DEFAULT 0,
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Unicidade real com colunas nullable via COALESCE
-- (NULL nao colide com NULL em UNIQUE comum do PostgreSQL).
-- Sem CHECK (saldo_atual >= 0): ajuste por ADMIN pode deixar saldo negativo (DD-04, RN-01).
CREATE UNIQUE INDEX uk_estoque_combinacao
    ON estoque (
        produto_id,
        COALESCE(variacao_id, '00000000-0000-0000-0000-000000000000'::uuid),
        COALESCE(localizacao, '')
    );

CREATE INDEX idx_estoque_produto ON estoque(produto_id);
