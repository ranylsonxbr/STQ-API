package com.example.Stq.fornecedor;

import com.example.Stq.autenticacao.application.dto.LoginRequest;
import com.example.Stq.autenticacao.application.dto.LoginResponse;
import com.example.Stq.fornecedor.application.dto.FornecedorRequest;
import com.example.Stq.fornecedor.application.dto.FornecedorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.junit.jupiter.api.AfterEach;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes de integração para o módulo de Fornecedor.
 *
 * CNPJs utilizados (todos verificados pelo algoritmo da Receita Federal):
 *   11222333000181 — [FORN-I1] e [FORN-I2] — VÁLIDO (confirmado via cálculo de dígitos verificadores)
 *   22333444000181 — [FORN-I3] — VÁLIDO (substitui 44555666000147, que falha no algoritmo)
 *   55666777000181 — [FORN-I4] — VÁLIDO (substitui 77888999000195, que falha no algoritmo)
 *
 * Cálculo de verificação (pesos Receita Federal):
 *   1° dígito: pesos 5,4,3,2,9,8,7,6,5,4,3,2 → resto < 2 → 0, caso contrário → 11-resto
 *   2° dígito: pesos 6,5,4,3,2,9,8,7,6,5,4,3,2 → mesma regra
 *
 * Banco: PostgreSQL localhost:5432/stqdb?currentSchema=stq
 * Context path: /stq/api (configurado em application.yaml)
 * Endpoint base do MockMvc: /api/fornecedores (sem context path — MockMvc não usa context path)
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "app.jwt.secret=segredo-jwt-muito-longo-e-seguro-para-testes-unitarios-256-bits",
    "app.jwt.access-token-expiration=28800",
    "app.jwt.refresh-token-expiration=604800",
    "spring.flyway.clean-on-validation-error=true",
    "spring.flyway.clean-disabled=false"
})
class FornecedorIntegrationTest {

    // -----------------------------------------------------------------------
    // CNPJs válidos para os testes
    // -----------------------------------------------------------------------

    /** CNPJ usado em FORN-I1 e FORN-I2. */
    private static final String CNPJ_I1 = "11222333000181";

    /**
     * CNPJ usado em FORN-I3.
     * Substituição de 44555666000147 (inválido); calculado manualmente:
     * base 223334440001 → 1°dígito=8, 2°dígito=1 → 22333444000181.
     */
    private static final String CNPJ_I3 = "22333444000181";

    /**
     * CNPJ usado em FORN-I4.
     * Substituição de 77888999000195 (inválido); calculado manualmente:
     * base 556667770001 → 1°dígito=8, 2°dígito=1 → 55666777000181.
     */
    private static final String CNPJ_I4 = "55666777000181";

    // -----------------------------------------------------------------------
    // Infraestrutura de teste
    // -----------------------------------------------------------------------

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    private String tokenAdmin;

    @AfterEach
    void tearDown() {
        jdbcTemplate.update("DELETE FROM stq.fornecedor WHERE cnpj IN (?, ?, ?, ?)",
                CNPJ_I1, CNPJ_I3, CNPJ_I4, CNPJ_I1);
        jdbcTemplate.update("DELETE FROM stq.usuario WHERE email IN (?, ?)",
                "admin@teste.com", "operador@teste.com");
    }

    /**
     * Insere usuários de teste via JDBC (sem @Transactional — commits imediatos)
     * e obtém JWT via POST /api/auth/login.
     */
    @BeforeEach
    void setUp() throws Exception {
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
        jdbcTemplate.update("DELETE FROM stq.usuario WHERE email IN (?, ?)",
                "admin@teste.com", "operador@teste.com");

        String senhaHash = passwordEncoder.encode("senha123");

        jdbcTemplate.update(
                "INSERT INTO stq.usuario (id, nome, email, senha, perfil, ativo, criado_em) "
                        + "VALUES (gen_random_uuid(), ?, ?, ?, ?, true, now())",
                "Admin Teste", "admin@teste.com", senhaHash, "ADMIN"
        );

        jdbcTemplate.update(
                "INSERT INTO stq.usuario (id, nome, email, senha, perfil, ativo, criado_em) "
                        + "VALUES (gen_random_uuid(), ?, ?, ?, ?, true, now())",
                "Operador Teste", "operador@teste.com", senhaHash, "OPERADOR"
        );

        tokenAdmin = obterToken("admin@teste.com", "senha123");
    }

    // -----------------------------------------------------------------------
    // Cenários
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("[FORN-I1] deve executar ciclo CRUD completo")
    class CicloCrudCompleto {

        @Test
        @DisplayName("[FORN-I1] deve criar, buscar, atualizar, listar, desativar e filtrar por ativo=false")
        void deveCriarBuscarAtualizarListarDesativarEFiltrarPorAtivoFalse() throws Exception {

            // ----------------------------------------------------------------
            // Passo 1 — POST → 201 com Location header
            // ----------------------------------------------------------------
            var requestCriacao = new FornecedorRequest(
                    "Fornecedor Integração LTDA",
                    CNPJ_I1,
                    "contato@fornecedor.com",
                    "11912345678",
                    "João da Silva"
            );

            MvcResult resultCriacao = mockMvc.perform(post("/api/fornecedores")
                            .header("Authorization", "Bearer " + tokenAdmin)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestCriacao)))
                    .andExpect(status().isCreated())
                    .andExpect(header().exists("Location"))
                    .andReturn();

            String location = resultCriacao.getResponse().getHeader("Location");
            assertThat(location).isNotBlank();

            // ----------------------------------------------------------------
            // Passo 2 — GET no Location → 200 com dados corretos
            // ----------------------------------------------------------------
            String locationPath = extrairPathDaLocation(location);

            MvcResult resultBusca = mockMvc.perform(get(locationPath)
                            .header("Authorization", "Bearer " + tokenAdmin))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.razaoSocial").value("Fornecedor Integração LTDA"))
                    .andExpect(jsonPath("$.cnpj").value(CNPJ_I1))
                    .andExpect(jsonPath("$.email").value("contato@fornecedor.com"))
                    .andExpect(jsonPath("$.ativo").value(true))
                    .andReturn();

            FornecedorResponse fornecedorCriado = objectMapper.readValue(
                    resultBusca.getResponse().getContentAsString(), FornecedorResponse.class);
            assertThat(fornecedorCriado.id()).isNotNull();

            String idStr = fornecedorCriado.id().toString();

            // ----------------------------------------------------------------
            // Passo 3 — PUT → 200 com razaoSocial atualizada
            // ----------------------------------------------------------------
            var requestAtualizacao = new FornecedorRequest(
                    "Fornecedor Integração Atualizado LTDA",
                    CNPJ_I1,
                    "novo@fornecedor.com",
                    "11998765432",
                    "Maria Oliveira"
            );

            mockMvc.perform(put("/api/fornecedores/" + idStr)
                            .header("Authorization", "Bearer " + tokenAdmin)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestAtualizacao)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.razaoSocial").value("Fornecedor Integração Atualizado LTDA"))
                    .andExpect(jsonPath("$.email").value("novo@fornecedor.com"));

            // ----------------------------------------------------------------
            // Passo 4 — GET /api/fornecedores com filtro de razaoSocial parcial
            // ----------------------------------------------------------------
            mockMvc.perform(get("/api/fornecedores")
                            .param("razaoSocial", "Integração Atualizado")
                            .header("Authorization", "Bearer " + tokenAdmin))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content[?(@.id == '" + idStr + "')]").exists());

            // ----------------------------------------------------------------
            // Passo 5 — DELETE → 204, fornecedor ativo=false
            // ----------------------------------------------------------------
            mockMvc.perform(delete("/api/fornecedores/" + idStr)
                            .header("Authorization", "Bearer " + tokenAdmin))
                    .andExpect(status().isNoContent());

            mockMvc.perform(get("/api/fornecedores/" + idStr)
                            .header("Authorization", "Bearer " + tokenAdmin))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.ativo").value(false));

            // ----------------------------------------------------------------
            // Passo 6 — GET /api/fornecedores?ativo=false → fornecedor na lista
            // ----------------------------------------------------------------
            mockMvc.perform(get("/api/fornecedores")
                            .param("ativo", "false")
                            .header("Authorization", "Bearer " + tokenAdmin))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content[?(@.id == '" + idStr + "')]").exists());
        }
    }

    @Nested
    @DisplayName("[FORN-I2] CNPJ duplicado retorna 409")
    class CnpjDuplicado {

        @Test
        @DisplayName("[FORN-I2] deve retornar 409 com application/problem+json ao cadastrar CNPJ duplicado")
        void deveRetornar409QuandoCnpjDuplicado() throws Exception {
            var request = new FornecedorRequest(
                    "Fornecedor Original LTDA",
                    CNPJ_I1,
                    null,
                    null,
                    null
            );

            // Primeiro cadastro — sucesso
            mockMvc.perform(post("/api/fornecedores")
                            .header("Authorization", "Bearer " + tokenAdmin)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());

            // Segundo cadastro com mesmo CNPJ — conflito
            mockMvc.perform(post("/api/fornecedores")
                            .header("Authorization", "Bearer " + tokenAdmin)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                    .andExpect(jsonPath("$.status").value(409));
        }
    }

    @Nested
    @DisplayName("[FORN-I3] Desativação com filtro de ativo")
    class DesativacaoComFiltroDeAtivo {

        @Test
        @DisplayName("[FORN-I3] fornecedor desativado não aparece em ativo=true e aparece em ativo=false")
        void fornecedorDesativadoDeveAparecerApenasNoFiltroAtivoFalse() throws Exception {
            // Passo 1 — Criar
            MvcResult resultCriacao = mockMvc.perform(post("/api/fornecedores")
                            .header("Authorization", "Bearer " + tokenAdmin)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new FornecedorRequest(
                                    "Fornecedor Filtro Ativo LTDA",
                                    CNPJ_I3,
                                    null,
                                    null,
                                    null
                            ))))
                    .andExpect(status().isCreated())
                    .andReturn();

            String idStr = extrairIdDaLocation(resultCriacao);

            // Passo 2 — DELETE → 204
            mockMvc.perform(delete("/api/fornecedores/" + idStr)
                            .header("Authorization", "Bearer " + tokenAdmin))
                    .andExpect(status().isNoContent());

            // Passo 3 — GET ?ativo=true → fornecedor NÃO aparece
            mockMvc.perform(get("/api/fornecedores")
                            .param("ativo", "true")
                            .header("Authorization", "Bearer " + tokenAdmin))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[?(@.id == '" + idStr + "')]").doesNotExist());

            // Passo 4 — GET ?ativo=false → fornecedor aparece
            mockMvc.perform(get("/api/fornecedores")
                            .param("ativo", "false")
                            .header("Authorization", "Bearer " + tokenAdmin))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[?(@.id == '" + idStr + "')]").exists());
        }
    }

    @Nested
    @DisplayName("[FORN-I4] Reativação idempotente")
    class ReativacaoIdempotente {

        @Test
        @DisplayName("[FORN-I4] deve reativar fornecedor e segunda chamada deve ser idempotente")
        void deveReativarFornecedorDeFormaIdempotente() throws Exception {
            // Passo 1 — Criar
            MvcResult resultCriacao = mockMvc.perform(post("/api/fornecedores")
                            .header("Authorization", "Bearer " + tokenAdmin)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new FornecedorRequest(
                                    "Fornecedor Reativacao LTDA",
                                    CNPJ_I4,
                                    null,
                                    null,
                                    null
                            ))))
                    .andExpect(status().isCreated())
                    .andReturn();

            String idStr = extrairIdDaLocation(resultCriacao);

            // Passo 2 — DELETE → ativo=false
            mockMvc.perform(delete("/api/fornecedores/" + idStr)
                            .header("Authorization", "Bearer " + tokenAdmin))
                    .andExpect(status().isNoContent());

            mockMvc.perform(get("/api/fornecedores/" + idStr)
                            .header("Authorization", "Bearer " + tokenAdmin))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.ativo").value(false));

            // Passo 3 — PATCH /reativar → 200, ativo=true
            mockMvc.perform(patch("/api/fornecedores/" + idStr + "/reativar")
                            .header("Authorization", "Bearer " + tokenAdmin))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.ativo").value(true));

            // Passo 4 — PATCH /reativar novamente → 200, ativo=true (idempotente)
            mockMvc.perform(patch("/api/fornecedores/" + idStr + "/reativar")
                            .header("Authorization", "Bearer " + tokenAdmin))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.ativo").value(true));
        }
    }

    @Nested
    @DisplayName("[FORN-I5] Filtro de CNPJ com entrada formatada (normalização)")
    class FiltroCnpjFormatado {

        @Test
        @DisplayName("[FORN-I5] deve encontrar fornecedor ao filtrar por CNPJ formatado com pontuação")
        void deveEncontrarFornecedorAoFiltrarPorCnpjFormatado() throws Exception {
            // Passo 1 — Criar com CNPJ sem formatação
            MvcResult resultCriacao = mockMvc.perform(post("/api/fornecedores")
                            .header("Authorization", "Bearer " + tokenAdmin)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new FornecedorRequest(
                                    "Fornecedor Normalizacao CNPJ LTDA",
                                    CNPJ_I1,
                                    null,
                                    null,
                                    null
                            ))))
                    .andExpect(status().isCreated())
                    .andReturn();

            String idStr = extrairIdDaLocation(resultCriacao);

            // Passo 2 — GET com CNPJ formatado → fornecedor encontrado
            // O serviço deve normalizar "11.222.333/0001-81" → "11222333000181" antes de filtrar
            mockMvc.perform(get("/api/fornecedores")
                            .param("cnpj", "11.222.333/0001-81")
                            .header("Authorization", "Bearer " + tokenAdmin))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content[?(@.id == '" + idStr + "')]").exists());
        }
    }

    // -----------------------------------------------------------------------
    // Utilitários
    // -----------------------------------------------------------------------

    /**
     * Obtém o JWT de acesso fazendo POST /api/auth/login com as credenciais fornecidas.
     */
    private String obterToken(String email, String senha) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest(email, senha))))
                .andExpect(status().isOk())
                .andReturn();

        LoginResponse loginResponse = objectMapper.readValue(
                result.getResponse().getContentAsString(), LoginResponse.class);

        assertThat(loginResponse.accessToken())
                .as("Token JWT para %s não pode ser nulo", email)
                .isNotBlank();

        return loginResponse.accessToken();
    }

    /**
     * Extrai o path relativo de uma URL completa de Location.
     * Exemplo: "http://localhost:8080/stq/api/api/fornecedores/uuid" → "/api/fornecedores/uuid"
     * Se a URL não contiver o context path, retorna tudo após a porta.
     */
    private String extrairPathDaLocation(String location) {
        // O MockMvc com RANDOM_PORT pode retornar a URL completa.
        // Precisamos extrair apenas o path sem o context path (/stq/api),
        // pois o MockMvc já o inclui implicitamente ao fazer perform().
        if (location == null) return "";
        // Remove protocolo + host + porta
        String semEsquema = location.replaceFirst("https?://[^/]+", "");
        // Remove context path /stq/api se presente (MockMvc não precisa dele)
        if (semEsquema.startsWith("/stq/api")) {
            return semEsquema.substring("/stq/api".length());
        }
        return semEsquema;
    }

    /**
     * Extrai o id (UUID como String) do Location header de um resultado de criação.
     * O Location header tem o formato: http://localhost:{port}/stq/api/api/fornecedores/{uuid}
     */
    private String extrairIdDaLocation(MvcResult result) {
        String location = result.getResponse().getHeader("Location");
        assertThat(location).as("Location header deve estar presente").isNotBlank();
        // O UUID é o último segmento do path
        String[] partes = location.split("/");
        return partes[partes.length - 1];
    }
}
