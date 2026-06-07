package com.example.Stq.fornecedor.domain;

public final class CnpjValidator {
    private CnpjValidator() {}

    public static String normalizar(String cnpj) {
        return cnpj == null ? null : cnpj.replaceAll("\\D", "");
    }

    public static boolean isValido(String cnpjNormalizado) {
        if (cnpjNormalizado == null || cnpjNormalizado.length() != 14) return false;
        if (cnpjNormalizado.chars().distinct().count() == 1) return false;
        int dv1 = calcularDigito(cnpjNormalizado, 12, new int[]{5,4,3,2,9,8,7,6,5,4,3,2});
        int dv2 = calcularDigito(cnpjNormalizado, 13, new int[]{6,5,4,3,2,9,8,7,6,5,4,3,2});
        return dv1 == (cnpjNormalizado.charAt(12) - '0')
            && dv2 == (cnpjNormalizado.charAt(13) - '0');
    }

    private static int calcularDigito(String cnpj, int len, int[] pesos) {
        int soma = 0;
        for (int i = 0; i < len; i++) soma += (cnpj.charAt(i) - '0') * pesos[i];
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }
}
