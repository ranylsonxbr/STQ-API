CREATE TABLE usuario (
    id         UUID         NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    nome       VARCHAR(100) NOT NULL,
    email      VARCHAR(255) NOT NULL UNIQUE,
    senha      VARCHAR(255) NOT NULL,
    perfil     VARCHAR(20)  NOT NULL CHECK (perfil IN ('ADMIN', 'OPERADOR', 'VISUALIZADOR')),
    ativo      BOOLEAN      NOT NULL DEFAULT TRUE,
    criado_em  TIMESTAMPTZ  NOT NULL DEFAULT now()
);
