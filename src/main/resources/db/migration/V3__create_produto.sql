CREATE TABLE produto (
    id             UUID          NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    sku            VARCHAR(20)   NOT NULL UNIQUE,
    nome           VARCHAR(120)  NOT NULL,
    descricao      VARCHAR(1000) NULL,
    categoria_id   UUID          NOT NULL REFERENCES categoria(id),
    unidade_medida VARCHAR(4)    NOT NULL CHECK (unidade_medida IN ('UN','KG','LT','MT','CX','PC')),
    estoque_minimo INTEGER       NOT NULL DEFAULT 0 CHECK (estoque_minimo >= 0),
    ativo          BOOLEAN       NOT NULL DEFAULT TRUE,
    criado_em      TIMESTAMPTZ   NOT NULL DEFAULT now()
);

CREATE INDEX idx_produto_categoria ON produto(categoria_id);
CREATE INDEX idx_produto_ativo_nome ON produto(ativo, nome);
