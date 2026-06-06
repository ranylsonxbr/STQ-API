package com.example.Stq.fornecedor.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CnpjValidator")
class CnpjValidatorTest {

    private static final String CNPJ_VALIDO_NORMALIZADO = "11222333000181";
    private static final String CNPJ_VALIDO_FORMATADO = "11.222.333/0001-81";

    @Nested
    @DisplayName("isValido")
    class IsValido {

        @Test
        @DisplayName("deve retornar true para CNPJ válido já normalizado")
        void deveRetornarTrueParaCnpjValidoNormalizado() {
            assertThat(CnpjValidator.isValido(CNPJ_VALIDO_NORMALIZADO)).isTrue();
        }

        @Test
        @DisplayName("deve retornar true para CNPJ válido após normalizar com formatação")
        void deveRetornarTrueParaCnpjValidoFormatadoAposNormalizar() {
            String normalizado = CnpjValidator.normalizar(CNPJ_VALIDO_FORMATADO);
            assertThat(CnpjValidator.isValido(normalizado)).isTrue();
        }

        @Test
        @DisplayName("deve retornar false para CNPJ com dígito verificador errado")
        void deveRetornarFalseParaCnpjComDigitoVerificadorErrado() {
            // último dígito alterado de 1 para 2
            assertThat(CnpjValidator.isValido("11222333000182")).isFalse();
        }

        @Test
        @DisplayName("deve retornar false para CNPJ com comprimento diferente de 14 após normalizar")
        void deveRetornarFalseParaCnpjComComprimentoInvalido() {
            assertThat(CnpjValidator.isValido("1122233300018")).isFalse();   // 13 dígitos
            assertThat(CnpjValidator.isValido("112223330001810")).isFalse(); // 15 dígitos
        }

        @Test
        @DisplayName("deve retornar false para CNPJ com todos os dígitos iguais")
        void deveRetornarFalseParaCnpjComTodosDigitosIguais() {
            assertThat(CnpjValidator.isValido("00000000000000")).isFalse();
            assertThat(CnpjValidator.isValido("11111111111111")).isFalse();
        }

        @Test
        @DisplayName("deve retornar false para CNPJ null")
        void deveRetornarFalseParaCnpjNull() {
            assertThat(CnpjValidator.isValido(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("normalizar")
    class Normalizar {

        @Test
        @DisplayName("deve remover pontuação do CNPJ formatado")
        void deveRemoverPontuacaoDoCnpjFormatado() {
            assertThat(CnpjValidator.normalizar(CNPJ_VALIDO_FORMATADO))
                    .isEqualTo(CNPJ_VALIDO_NORMALIZADO);
        }

        @Test
        @DisplayName("deve retornar null quando CNPJ for null")
        void deveRetornarNullQuandoCnpjForNull() {
            assertThat(CnpjValidator.normalizar(null)).isNull();
        }

        @Test
        @DisplayName("deve retornar apenas dígitos sem alterar CNPJ já normalizado")
        void deveRetornarMesmaCadeiaDeCnpjJaNormalizado() {
            assertThat(CnpjValidator.normalizar(CNPJ_VALIDO_NORMALIZADO))
                    .isEqualTo(CNPJ_VALIDO_NORMALIZADO);
        }
    }
}
