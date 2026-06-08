package com.example.Stq.relatorio.application.dto;

import java.math.BigDecimal;
import java.util.List;

public record RelatorioPedidos(List<PedidoCompraItem> dados, BigDecimal totalGasto) {}
