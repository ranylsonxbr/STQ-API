CREATE TABLE fornecedor (
    id             UUID         NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    razao_social   VARCHAR(150) NOT NULL,
    cnpj           VARCHAR(14)  NOT NULL UNIQUE,
    email          VARCHAR(150),
    telefone       VARCHAR(20),
    contato        VARCHAR(100),
    ativo          BOOLEAN      NOT NULL DEFAULT TRUE,
    criado_em      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    atualizado_em  TIMESTAMPTZ,
    atualizado_por UUID         REFERENCES usuario (id)
);

CREATE INDEX idx_fornecedor_razao_social ON fornecedor (lower(razao_social));
CREATE INDEX idx_fornecedor_ativo        ON fornecedor (ativo);
