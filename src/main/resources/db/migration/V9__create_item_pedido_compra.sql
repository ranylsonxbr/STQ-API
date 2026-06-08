CREATE TABLE item_pedido_compra (
    id UUID PRIMARY KEY,
    pedido_compra_id UUID NOT NULL REFERENCES pedido_compra(id) ON DELETE CASCADE,
    produto_id UUID NOT NULL REFERENCES produto(id),
    variacao_id UUID REFERENCES variacao_produto(id),
    quantidade INTEGER NOT NULL,
    preco_unitario NUMERIC(15,2) NOT NULL,
    subtotal NUMERIC(15,2) NOT NULL,
    CONSTRAINT uk_item_pedido_produto_variacao UNIQUE (pedido_compra_id, produto_id, variacao_id)
);

CREATE INDEX idx_item_pedido_pedido ON item_pedido_compra(pedido_compra_id);
CREATE INDEX idx_item_pedido_produto ON item_pedido_compra(produto_id);
