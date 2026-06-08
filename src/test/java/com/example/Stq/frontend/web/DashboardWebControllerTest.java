package com.example.Stq.frontend.web;

import com.example.Stq.fornecedor.application.dto.FornecedorResponse;
import com.example.Stq.fornecedor.application.services.FornecedorService;
import com.example.Stq.pedidocompra.application.dto.PedidoCompraResponse;
import com.example.Stq.pedidocompra.application.services.PedidoCompraService;
import com.example.Stq.produto.application.dto.ProdutoResponse;
import com.example.Stq.produto.application.services.ProdutoService;
import com.example.Stq.relatorio.application.dto.EstoqueAtualItem;
import com.example.Stq.relatorio.application.services.RelatorioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@ExtendWith(MockitoExtension.class)
@DisplayName("DashboardWebController")
class DashboardWebControllerTest {

    @Mock
    private RelatorioService relatorioService;
    @Mock
    private ProdutoService produtoService;
    @Mock
    private FornecedorService fornecedorService;
    @Mock
    private PedidoCompraService pedidoCompraService;

    @InjectMocks
    private DashboardWebController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Nested
    @DisplayName("GET /web/dashboard")
    class Dashboard {

        @Test
        @DisplayName("[FW-D1] deve retornar view 'dashboard/index' com atributos de contadores")
        void deveRetornarViewDashboardComContadores() throws Exception {
            // Arrange
            var alerta = new EstoqueAtualItem("PRD-001", "Produto X", "UN", 2, 10, null);
            when(relatorioService.alertas()).thenReturn(List.of(alerta, alerta));

            Page<ProdutoResponse> pageProdutos = new PageImpl<>(List.of(), PageRequest.of(0, 1), 5L);
            when(produtoService.listar(any(), any(), any(), any())).thenReturn(pageProdutos);

            Page<FornecedorResponse> pageFornecedores = new PageImpl<>(List.of(), PageRequest.of(0, 1), 3L);
            when(fornecedorService.listar(any(), any())).thenReturn(pageFornecedores);

            // pedidos PENDENTE e APROVADO
            Page<PedidoCompraResponse> pagePendente = new PageImpl<>(List.of(), PageRequest.of(0, 1), 4L);
            Page<PedidoCompraResponse> pageAprovado = new PageImpl<>(List.of(), PageRequest.of(0, 1), 2L);
            when(pedidoCompraService.listar(any(), any()))
                    .thenReturn(pagePendente)
                    .thenReturn(pageAprovado);

            // Act + Assert
            mockMvc.perform(get("/web/dashboard"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("dashboard/index"))
                    .andExpect(model().attributeExists("totalAlertas"))
                    .andExpect(model().attributeExists("totalProdutosAtivos"))
                    .andExpect(model().attributeExists("totalFornecedoresAtivos"))
                    .andExpect(model().attributeExists("totalPedidosAbertos"))
                    .andExpect(model().attribute("totalAlertas", 2))
                    .andExpect(model().attribute("totalProdutosAtivos", 5L))
                    .andExpect(model().attribute("totalFornecedoresAtivos", 3L))
                    .andExpect(model().attribute("totalPedidosAbertos", 6L));
        }

        @Test
        @DisplayName("[FW-D2] deve exibir zero alertas quando estoque sem itens criticos")
        void deveExibirZeroAlertasQuandoSemItensAbaixoMinimo() throws Exception {
            // Arrange
            when(relatorioService.alertas()).thenReturn(List.of());
            Page<ProdutoResponse> pageVaziaProd = new PageImpl<>(List.of(), PageRequest.of(0, 1), 0L);
            Page<FornecedorResponse> pageVaziaForn = new PageImpl<>(List.of(), PageRequest.of(0, 1), 0L);
            Page<PedidoCompraResponse> pageVaziaPed = new PageImpl<>(List.of(), PageRequest.of(0, 1), 0L);
            when(produtoService.listar(any(), any(), any(), any())).thenReturn(pageVaziaProd);
            when(fornecedorService.listar(any(), any())).thenReturn(pageVaziaForn);
            when(pedidoCompraService.listar(any(), any())).thenReturn(pageVaziaPed);

            // Act + Assert
            mockMvc.perform(get("/web/dashboard"))
                    .andExpect(status().isOk())
                    .andExpect(model().attribute("totalAlertas", 0))
                    .andExpect(model().attribute("totalPedidosAbertos", 0L));
        }
    }
}
