package com.example.Stq.relatorio.infra;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CsvHelper")
class CsvHelperTest {

    @Nested
    @DisplayName("toCsv")
    class ToCsv {

        @Test
        @DisplayName("[CSV-01] sem linhas deve retornar apenas cabeçalho")
        void semLinhasDeveRetornarApenasCabecalho() {
            String csv = CsvHelper.toCsv(new String[]{"A", "B"}, List.<String[]>of());
            assertThat(csv).isEqualTo("A;B\n");
        }

        @Test
        @DisplayName("[CSV-02] com linhas deve gerar CSV completo")
        void comLinhasDeveGerarCsvCompleto() {
            String csv = CsvHelper.toCsv(
                    new String[]{"SKU", "Nome"},
                    List.<String[]>of(new String[]{"PRD-001", "Produto A"})
            );
            assertThat(csv).isEqualTo("SKU;Nome\nPRD-001;Produto A\n");
        }

        @Test
        @DisplayName("[CSV-03] campo com ponto-e-vírgula deve ser envolvido em aspas")
        void campoComPontoEVirgulaDeveSerEnvolvidoEmAspas() {
            String csv = CsvHelper.toCsv(
                    new String[]{"Nome"},
                    List.<String[]>of(new String[]{"Empresa; Ltda"})
            );
            assertThat(csv).contains("\"Empresa; Ltda\"");
        }

        @Test
        @DisplayName("[CSV-04] campo com aspas internas deve escapar com aspas duplas")
        void campoComAspasInternasDeveEscapar() {
            String csv = CsvHelper.toCsv(
                    new String[]{"Obs"},
                    List.<String[]>of(new String[]{"diz \"olá\""})
            );
            assertThat(csv).contains("\"diz \"\"olá\"\"\"");
        }

        @Test
        @DisplayName("[CSV-05] campo nulo deve ser tratado como vazio")
        void campoNuloDeveSerVazio() {
            List<String[]> linhas = new java.util.ArrayList<>();
            linhas.add(new String[]{"valor", null});
            String csv = CsvHelper.toCsv(new String[]{"A", "B"}, linhas);
            assertThat(csv).isEqualTo("A;B\nvalor;\n");
        }
    }
}
