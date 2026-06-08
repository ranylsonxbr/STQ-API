CREATE TABLE pedido_compra (
    id UUID PRIMARY KEY,
    fornecedor_id UUID NOT NULL REFERENCES fornecedor(id),
    status VARCHAR(20) NOT NULL,
    data_emissao DATE NOT NULL,
    data_previsao_entrega DATE,
    observacao VARCHAR(500),
    total_pedido NUMERIC(15,2) NOT NULL DEFAULT 0,
    criado_por UUID NOT NULL REFERENCES usuario(id),
    criado_em TIMESTAMP NOT NULL
);

CREATE INDEX idx_pedido_compra_fornecedor ON pedido_compra(fornecedor_id);
CREATE INDEX idx_pedido_compra_status ON pedido_compra(status);
