package com.example.Stq.produto.application.services;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class SkuGenerator {

    private static final String ALFABETO = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private final SecureRandom random = new SecureRandom();

    public String gerarSkuProduto() {
        return "PRD-" + sufixoAleatorio(6);
    }

    public String gerarSkuVariacao(String skuProduto) {
        return skuProduto + "-" + sufixoAleatorio(3);
    }

    private String sufixoAleatorio(int tamanho) {
        StringBuilder sb = new StringBuilder(tamanho);
        for (int i = 0; i < tamanho; i++) {
            sb.append(ALFABETO.charAt(random.nextInt(ALFABETO.length())));
        }
        return sb.toString();
    }
}
