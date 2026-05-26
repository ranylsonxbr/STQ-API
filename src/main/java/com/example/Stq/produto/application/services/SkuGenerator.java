package com.example.Stq.produto.application.services;

import com.example.Stq.produto.domain.exception.SkuColisaoException;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class SkuGenerator {

    private static final String ALFABETO = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int TENTATIVAS_MAX = 5;
    private final SecureRandom random = new SecureRandom();

    public String gerarSkuProduto() {
        return "PRD-" + sufixoAleatorio(6);
    }

    public String gerarSkuVariacao(String skuProduto) {
        return skuProduto + "-" + sufixoAleatorio(3);
    }

    public interface SkuSupplier {
        String get();
    }

    public String gerarComRetry(SkuSupplier supplier, java.util.function.Predicate<String> jaExiste) {
        for (int i = 0; i < TENTATIVAS_MAX; i++) {
            String sku = supplier.get();
            if (!jaExiste.test(sku)) {
                return sku;
            }
        }
        throw new SkuColisaoException();
    }

    private String sufixoAleatorio(int tamanho) {
        StringBuilder sb = new StringBuilder(tamanho);
        for (int i = 0; i < tamanho; i++) {
            sb.append(ALFABETO.charAt(random.nextInt(ALFABETO.length())));
        }
        return sb.toString();
    }
}
