CREATE TABLE variacao_produto (
    id           UUID         NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    produto_id   UUID         NOT NULL REFERENCES produto(id),
    atributo     VARCHAR(60)  NOT NULL,
    valor        VARCHAR(120) NOT NULL,
    sku_variacao VARCHAR(40)  NOT NULL UNIQUE,
    ativo        BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_variacao_por_produto UNIQUE (produto_id, atributo, valor)
);

CREATE INDEX idx_variacao_produto ON variacao_produto(produto_id);
