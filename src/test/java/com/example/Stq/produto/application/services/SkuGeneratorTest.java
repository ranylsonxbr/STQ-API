package com.example.Stq.produto.application.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SkuGenerator")
class SkuGeneratorTest {

    private final SkuGenerator skuGenerator = new SkuGenerator();

    @Test
    @DisplayName("deve gerar SKU de produto no formato PRD-XXXXXX")
    void deveGerarSkuProdutoNoFormatoCorreto() {
        String sku = skuGenerator.gerarSkuProduto();
        assertThat(sku).matches("PRD-[A-Z2-9]{6}");
    }

    @Test
    @DisplayName("deve gerar SKU de variacao derivado do SKU do produto")
    void deveGerarSkuVariacaoDerivaDoSkuProduto() {
        String skuVariacao = skuGenerator.gerarSkuVariacao("PRD-ABC123");
        assertThat(skuVariacao).startsWith("PRD-ABC123-");
        assertThat(skuVariacao).matches("PRD-ABC123-[A-Z2-9]{3}");
    }

    @Test
    @DisplayName("deve gerar 1000 SKUs sem colisao")
    void deveGerar1000SkusSemColisao() {
        Set<String> skus = new HashSet<>();
        for (int i = 0; i < 1000; i++) {
            skus.add(skuGenerator.gerarSkuProduto());
        }
        assertThat(skus).hasSizeGreaterThan(990);
    }

    @Test
    @DisplayName("gerarComRetry deve retornar SKU quando nao ha colisao")
    void gerarComRetrySemColisao() {
        String sku = skuGenerator.gerarComRetry(skuGenerator::gerarSkuProduto, s -> false);
        assertThat(sku).matches("PRD-[A-Z2-9]{6}");
    }
}
