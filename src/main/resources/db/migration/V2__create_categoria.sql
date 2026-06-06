CREATE TABLE categoria (
    id               UUID        NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    nome             VARCHAR(60) NOT NULL,
    categoria_pai_id UUID        NULL REFERENCES categoria(id),
    ativo            BOOLEAN     NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_categoria_nome_por_pai UNIQUE NULLS NOT DISTINCT (nome, categoria_pai_id)
);

CREATE INDEX idx_categoria_pai ON categoria(categoria_pai_id);
