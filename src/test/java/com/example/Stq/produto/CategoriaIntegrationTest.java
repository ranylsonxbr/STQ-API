package com.example.Stq.produto;

import com.example.Stq.produto.application.dto.CategoriaCreateRequest;
import com.example.Stq.produto.application.dto.CategoriaUpdateRequest;
import com.example.Stq.produto.domain.Categoria;
import com.example.Stq.produto.infra.CategoriaJpaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.UUID;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("CategoriaIntegrationTest")
class CategoriaIntegrationTest {

    @Autowired WebApplicationContext wac;
    @Autowired CategoriaJpaRepository categoriaJpaRepository;

    private MockMvc mockMvc;
    private final ObjectMapper json = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac)
                .apply(springSecurity())
                .build();
        categoriaJpaRepository.deleteAll();
    }

    private Categoria salvarCategoria(String nome) {
        return categoriaJpaRepository.save(
                Categoria.builder().nome(nome).ativo(true).build());
    }

    @Nested
    @DisplayName("POST /api/categorias")
    class Criar {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("[US-A1] deve criar categoria raiz e retornar 201 com Location")
        void deveCriarCategoriaRaiz() throws Exception {
            mockMvc.perform(post("/api/categorias")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json.writeValueAsString(new CategoriaCreateRequest("Integração", null))))
                    .andExpect(status().isCreated())
                    .andExpect(header().exists("Location"))
                    .andExpect(jsonPath("$.nome").value("Integração"))
                    .andExpect(jsonPath("$.ativo").value(true));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("[US-A2] deve criar subcategoria vinculada ao pai")
        void deveCriarSubcategoria() throws Exception {
            Categoria pai = salvarCategoria("Pai Integração");

            mockMvc.perform(post("/api/categorias")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json.writeValueAsString(
                                    new CategoriaCreateRequest("Filho Integração", pai.getId()))))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.categoriaPaiId").value(pai.getId().toString()));
        }

        @Test
        @WithMockUser(roles = "VISUALIZADOR")
        @DisplayName("deve retornar 403 para VISUALIZADOR tentando criar categoria")
        void deveRetornar403ParaVisualizador() throws Exception {
            mockMvc.perform(post("/api/categorias")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json.writeValueAsString(new CategoriaCreateRequest("Teste", null))))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("deve retornar 400 para nome em branco")
        void deveRetornar400NomeEmBranco() throws Exception {
            mockMvc.perform(post("/api/categorias")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"nome\":\"\"}"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/categorias")
    class Listar {

        @Test
        @WithMockUser(roles = "VISUALIZADOR")
        @DisplayName("[US-A3] deve listar categorias paginadas")
        void deveListarCategorias() throws Exception {
            salvarCategoria("Cat Listar A");
            salvarCategoria("Cat Listar B");

            mockMvc.perform(get("/api/categorias"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.totalElements").value(2));
        }

        @Test
        @DisplayName("deve retornar 401 sem autenticacao")
        void deveRetornar401SemAuth() throws Exception {
            mockMvc.perform(get("/api/categorias"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /api/categorias/{id}")
    class BuscarPorId {

        @Test
        @WithMockUser(roles = "OPERADOR")
        @DisplayName("deve retornar categoria por ID")
        void deveRetornarCategoriaPorId() throws Exception {
            Categoria cat = salvarCategoria("Cat BuscarId");

            mockMvc.perform(get("/api/categorias/{id}", cat.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(cat.getId().toString()))
                    .andExpect(jsonPath("$.nome").value("Cat BuscarId"));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("deve retornar 404 para ID inexistente")
        void deveRetornar404() throws Exception {
            mockMvc.perform(get("/api/categorias/{id}", UUID.randomUUID()))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PUT /api/categorias/{id}")
    class Atualizar {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("deve atualizar nome da categoria")
        void deveAtualizarCategoria() throws Exception {
            Categoria cat = salvarCategoria("Cat Antes");

            mockMvc.perform(put("/api/categorias/{id}", cat.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json.writeValueAsString(new CategoriaUpdateRequest("Cat Depois", null))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.nome").value("Cat Depois"));
        }
    }

    @Nested
    @DisplayName("DELETE /api/categorias/{id}")
    class Desativar {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("[US-A5] deve desativar categoria sem produtos nem filhas")
        void deveDesativarCategoria() throws Exception {
            Categoria cat = salvarCategoria("Cat Desativar");

            mockMvc.perform(delete("/api/categorias/{id}", cat.getId()))
                    .andExpect(status().isNoContent());

            mockMvc.perform(get("/api/categorias/{id}", cat.getId()))
                    .andExpect(jsonPath("$.ativo").value(false));
        }
    }
}
