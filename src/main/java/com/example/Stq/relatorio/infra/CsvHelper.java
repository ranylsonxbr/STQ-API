package com.example.Stq.relatorio.infra;

import java.util.List;

public final class CsvHelper {

    private CsvHelper() {}

    public static String toCsv(String[] cabecalho, List<String[]> linhas) {
        var sb = new StringBuilder();
        appendLinha(sb, cabecalho);
        for (String[] linha : linhas) {
            appendLinha(sb, linha);
        }
        return sb.toString();
    }

    private static void appendLinha(StringBuilder sb, String[] campos) {
        for (int i = 0; i < campos.length; i++) {
            if (i > 0) sb.append(';');
            String valor = campos[i] == null ? "" : campos[i];
            if (valor.contains(";") || valor.contains("\"") || valor.contains("\n")) {
                sb.append('"').append(valor.replace("\"", "\"\"")).append('"');
            } else {
                sb.append(valor);
            }
        }
        sb.append('\n');
    }
}
