package com.example.Stq.fornecedor.application.controllers;

import com.example.Stq.autenticacao.domain.Perfil;
import com.example.Stq.autenticacao.domain.Usuario;
import com.example.Stq.autenticacao.infra.UsuarioDetails;
import com.example.Stq.config.GlobalExceptionHandler;
import com.example.Stq.fornecedor.application.dto.FornecedorRequest;
import com.example.Stq.fornecedor.application.dto.FornecedorResponse;
import com.example.Stq.fornecedor.application.services.FornecedorService;
import com.example.Stq.fornecedor.domain.FornecedorFiltro;
import com.example.Stq.fornecedor.domain.exception.FornecedorNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("FornecedorController")
class FornecedorControllerTest {

    @Mock
    private FornecedorService fornecedorService;

    @InjectMocks
    private FornecedorController controller;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String CNPJ_VALIDO = "11222333000181";
    private static final UUID FORNECEDOR_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID USUARIO_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(
                        new PageableHandlerMethodArgumentResolver(),
                        new AuthenticationPrincipalArgumentResolver())
                .build();
    }

    private void autenticarComo(Perfil perfil) {
        Usuario usuario = Usuario.builder()
                .id(USUARIO_ID)
                .nome("Teste")
                .email("teste@email.com")
                .senha("hash")
                .perfil(perfil)
                .ativo(true)
                .build();
        UsuarioDetails details = new UsuarioDetails(usuario);
        var auth = new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private FornecedorResponse responseFixture() {
        return new FornecedorResponse(
                FORNECEDOR_ID, "Razao Social Teste", CNPJ_VALIDO,
                "email@teste.com", "11999990000", "Contato",
                true, Instant.now(), Instant.now(), USUARIO_ID);
    }

    private FornecedorRequest requestValido() {
        return new FornecedorRequest("Razao Social Teste", CNPJ_VALIDO, "email@teste.com", "11999990000", "Contato");
    }

    @Nested
    @DisplayName("POST /api/fornecedores")
    class Criar {

        @Test
        @DisplayName("[FORN-A1] OPERADOR deve criar fornecedor e retornar 201 com header Location")
        void operadorDeveCriarFornecedorERetornar201ComLocation() throws Exception {
            autenticarComo(Perfil.OPERADOR);
            when(fornecedorService.criar(any(FornecedorRequest.class), eq(USUARIO_ID)))
                    .thenReturn(responseFixture());

            mockMvc.perform(post("/api/fornecedores")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestValido())))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", org.hamcrest.Matchers.containsString("/api/fornecedores/" + FORNECEDOR_ID)));
        }

        @Test
        @DisplayName("[FORN-A4] body inválido com razaoSocial vazia deve retornar 400 com application/problem+json")
        void bodyInvalidoDeveRetornar400ComProblemJson() throws Exception {
            autenticarComo(Perfil.OPERADOR);
            var requestInvalido = new FornecedorRequest("", CNPJ_VALIDO, "email@teste.com", null, null);

            mockMvc.perform(post("/api/fornecedores")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestInvalido)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400));
        }
    }

    @Nested
    @DisplayName("GET /api/fornecedores")
    class Listar {

        @Test
        @DisplayName("[FORN-A5] VISUALIZADOR deve listar fornecedores com filtros e paginação")
        void visualizadorDeveListarFornecedoresComFiltros() throws Exception {
            autenticarComo(Perfil.VISUALIZADOR);
            var page = new PageImpl<>(List.of(responseFixture()), PageRequest.of(0, 20), 1);
            when(fornecedorService.listar(any(FornecedorFiltro.class), any()))
                    .thenReturn(page);

            mockMvc.perform(get("/api/fornecedores")
                            .param("razaoSocial", "Razao")
                            .param("ativo", "true"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content[0].razaoSocial").value("Razao Social Teste"));

            verify(fornecedorService).listar(any(FornecedorFiltro.class), any());
        }
    }

    @Nested
    @DisplayName("GET /api/fornecedores/{id}")
    class BuscarPorId {

        @Test
        @DisplayName("[FORN-A6] id inexistente deve retornar 404 com application/problem+json")
        void idInexistenteDeveRetornar404ComProblemJson() throws Exception {
            autenticarComo(Perfil.VISUALIZADOR);
            UUID idInexistente = UUID.randomUUID();
            when(fornecedorService.buscarPorId(idInexistente))
                    .thenThrow(new FornecedorNotFoundException(idInexistente));

            mockMvc.perform(get("/api/fornecedores/{id}", idInexistente))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404));
        }
    }

    @Nested
    @DisplayName("PUT /api/fornecedores/{id}")
    class Atualizar {

        @Test
        @DisplayName("[FORN-A7] OPERADOR deve atualizar fornecedor e retornar 200")
        void operadorDeveAtualizarFornecedorERetornar200() throws Exception {
            autenticarComo(Perfil.OPERADOR);
            when(fornecedorService.atualizar(eq(FORNECEDOR_ID), any(FornecedorRequest.class),
                    eq(USUARIO_ID), eq(Perfil.OPERADOR)))
                    .thenReturn(responseFixture());

            mockMvc.perform(put("/api/fornecedores/{id}", FORNECEDOR_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestValido())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(FORNECEDOR_ID.toString()));
        }
    }

    @Nested
    @DisplayName("DELETE /api/fornecedores/{id}")
    class Desativar {

        @Test
        @DisplayName("[FORN-A8] OPERADOR deve desativar fornecedor e retornar 204")
        void operadorDeveDesativarFornecedorERetornar204() throws Exception {
            autenticarComo(Perfil.OPERADOR);

            mockMvc.perform(delete("/api/fornecedores/{id}", FORNECEDOR_ID))
                    .andExpect(status().isNoContent());

            verify(fornecedorService).desativar(FORNECEDOR_ID, USUARIO_ID);
        }
    }

    @Nested
    @DisplayName("PATCH /api/fornecedores/{id}/reativar")
    class Reativar {

        @Test
        @DisplayName("[FORN-A9] OPERADOR deve reativar fornecedor e retornar 200")
        void operadorDeveReativarFornecedorERetornar200() throws Exception {
            autenticarComo(Perfil.OPERADOR);
            when(fornecedorService.reativar(eq(FORNECEDOR_ID), eq(USUARIO_ID)))
                    .thenReturn(responseFixture());

            mockMvc.perform(patch("/api/fornecedores/{id}/reativar", FORNECEDOR_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.ativo").value(true));
        }
    }
}
