# estoque-ai

Plataforma de gerenciamento de estoque com controle de produtos, fornecedores, movimentações (entradas, saídas, transferências), pedidos de compra e relatórios. Interface web SSR construída com Thymeleaf e API REST documentada com Swagger/OpenAPI.

---

## Tecnologias

| Camada         | Tecnologia                               |
|----------------|------------------------------------------|
| Linguagem      | Java 17                                  |
| Framework      | Spring Boot 3.3.4                        |
| Build          | Maven 3.9                                |
| Banco de dados | PostgreSQL 17                            |
| Migrations     | Flyway                                   |
| Frontend       | Thymeleaf (SSR) + Bootstrap 5            |
| Segurança      | Spring Security + JWT (Auth0 java-jwt)   |
| Documentação   | SpringDoc OpenAPI / Swagger UI           |
| Testes         | JUnit 5 + Mockito + WireMock             |
| Cobertura      | JaCoCo (mínimo 80% em domain/application)|

---

## Funcionalidades

- **Autenticação** — login com JWT, refresh token e perfis de acesso (VISUALIZADOR, OPERADOR, ADMIN)
- **Produtos** — cadastro com geração automática de SKU (`PRD-XXXXXX`), categorias hierárquicas e variações (cor, tamanho, etc.)
- **Fornecedores** — cadastro com validação de CNPJ por dígito verificador
- **Estoque** — saldo em tempo real por produto, variação e localização física
- **Movimentações** — entradas, saídas, transferências entre localizações e ajustes de inventário (ADMIN)
- **Pedidos de compra** — fluxo completo: Rascunho → Pendente → Aprovado → Recebido, com recebimento gerando entradas atomicamente
- **Relatórios** — dashboard de alertas de estoque mínimo e exportação CSV

---

## Arquitetura

O projeto segue **Clean Architecture**. Cada módulo é isolado em três camadas:

```
modulo/
├── application/   # Controllers REST, DTOs (Java Records), Services
├── domain/        # Entidades, enums, regras de negócio, interfaces de repositório
└── infra/         # Implementações JPA, Specifications, adapters externos
```

A camada `domain` não possui nenhuma dependência de Spring, JPA ou infraestrutura. A inversão de dependência é feita via interfaces de repositório definidas no domínio e implementadas na camada `infra`.

### Módulos

| Módulo        | Responsabilidade                                   |
|---------------|----------------------------------------------------|
| autenticacao  | Login, JWT, perfis, refresh token                  |
| produto       | Produtos, categorias hierárquicas, variações, SKU  |
| fornecedor    | Cadastro e gestão de fornecedores                  |
| movimentacao  | Entradas, saídas, transferências, ajustes, estoque |
| pedido-compra | Fluxo de compra do rascunho ao recebimento         |
| relatorio     | Dashboard, alertas de estoque mínimo, export CSV   |
| frontend/web  | Controllers Thymeleaf, formulários SSR             |
| config        | Security, JwtFilter, beans globais, OpenAPI        |

---

## Pré-requisitos

- Java 17+
- Maven 3.9+
- Docker (para o PostgreSQL local)

---

## Como rodar localmente

### 1. Suba o banco de dados

```bash
docker compose -f docker-compose.dev.yml up -d
```

Isso sobe um PostgreSQL 17 na porta `5432` com:
- **Banco:** `stqdb`
- **Schema:** `stq`
- **Usuário/senha:** `postgres` / `postgres`

### 2. Execute a aplicação

```bash
./mvnw spring-boot:run
```

O Flyway roda automaticamente as migrations na primeira inicialização.

### 3. Acesse

| Recurso   | URL                                                          |
|-----------|--------------------------------------------------------------|
| Interface | http://localhost:8080/estoque-ai                             |
| Swagger   | http://localhost:8080/estoque-ai/api/swagger-ui/index.html  |
| OpenAPI   | http://localhost:8080/estoque-ai/api/v3/api-docs            |

---

## Primeiro acesso

O banco começa vazio — não há usuário pré-cadastrado. Siga os passos abaixo para criar o primeiro administrador:

### 1. Crie sua conta

Acesse a página de cadastro público (não requer login):

```
http://localhost:8080/estoque-ai/web/usuarios/registro
```

Preencha nome, e-mail e senha. A conta é criada com perfil **VISUALIZADOR** por padrão.

### 2. Promova para ADMIN no banco

Conecte ao PostgreSQL e execute:

```sql
UPDATE stq.usuario
SET perfil = 'ADMIN'
WHERE email = 'seu@email.com';
```

### 3. Faça login

Acesse `http://localhost:8080/estoque-ai/web/login` com as credenciais cadastradas.
Com o perfil `ADMIN` você terá acesso completo, incluindo a seção **Usuários** no menu lateral para criar e gerenciar as demais contas da equipe.

> **Demais usuários** (OPERADOR, VISUALIZADOR) são criados pelo ADMIN em
> `Administração → Usuários → Novo Usuário`, sem necessidade de acesso ao banco.

---

## Variáveis de ambiente

| Variável           | Padrão                                                         | Descrição                   |
|--------------------|----------------------------------------------------------------|-----------------------------|
| `DATASOURCE_URL`   | `jdbc:postgresql://localhost:5432/stqdb?currentSchema=stq`    | JDBC URL do banco           |
| `DATASOURCE_USER`  | `postgres`                                                     | Usuário do banco            |
| `DATASOURCE_PASS`  | `postgres`                                                     | Senha do banco              |
| `JWT_SECRET`       | `segredo-jwt-muito-longo-e-seguro-minimo-256-bits-para-hmac`  | Chave HMAC para JWT         |
| `APP_PROFILE`      | `local`                                                        | Perfil Spring ativo         |

---

## Perfis de acesso

| Ação                        | VISUALIZADOR | OPERADOR | ADMIN |
|-----------------------------|:---:|:---:|:---:|
| Ver produtos / fornecedores | ✓   | ✓   | ✓   |
| Criar / editar produtos     |     | ✓   | ✓   |
| Criar / editar fornecedores |     | ✓   | ✓   |
| Registrar entrada / saída   |     | ✓   | ✓   |
| Criar pedido de compra      |     | ✓   | ✓   |
| Aprovar pedido de compra    |     |     | ✓   |
| Ajuste de inventário        |     |     | ✓   |
| Ver relatórios              | ✓   | ✓   | ✓   |
| Gerenciar usuários          |     |     | ✓   |

---

## Fluxo do Pedido de Compra

```
RASCUNHO → PENDENTE → APROVADO → RECEBIDO
                    ↘ CANCELADO
```

- Cancelamento permitido apenas em `RASCUNHO` ou `PENDENTE`
- Recebimento gera movimentações de entrada para cada item de forma atômica (tudo ou nada)

---

## Regras de negócio

| Regra   | Descrição                                                                               |
|---------|-----------------------------------------------------------------------------------------|
| RN-01   | Saldo nunca fica negativo (exceto ajuste por ADMIN)                                     |
| RN-02   | Movimentações são imutáveis após criação                                                |
| RN-03   | SKU gerado automaticamente no formato `PRD-XXXXXX` e imutável                          |
| RN-04   | CNPJ de fornecedor validado por dígito verificador antes de persistir                  |
| RN-05   | Pedido só pode ser recebido se estiver com status `APROVADO`                            |
| RN-06   | Recebimento de pedido gera entradas para cada item atomicamente                         |
| RN-07   | Produto com pedido `PENDENTE` ou `APROVADO` não pode ser desativado                    |
| RN-08   | Fornecedor com pedido `PENDENTE` ou `APROVADO` não pode ser desativado                 |
| RN-09   | Ajuste de inventário exige perfil ADMIN                                                 |
| RN-10   | Aprovação de pedido exige perfil ADMIN                                                  |

---

## Convenções de API REST

| Operação             | Método  | Exemplo                            | Status de retorno         |
|----------------------|---------|------------------------------------|---------------------------|
| Criar                | POST    | `/api/produtos`                    | 201 + `Location` header   |
| Buscar por ID        | GET     | `/api/produtos/{id}`               | 200                       |
| Listar (paginado)    | GET     | `/api/produtos?page=0&size=20`     | 200                       |
| Atualizar            | PUT     | `/api/produtos/{id}`               | 200                       |
| Desativar (soft)     | DELETE  | `/api/produtos/{id}`               | 204                       |
| Transição de status  | PATCH   | `/api/pedidos-compra/{id}/aprovar` | 200                       |

Erros seguem o padrão **RFC 7807** (`application/problem+json`) via `ProblemDetail` do Spring 6.

---

## Migrations (Flyway)

As migrations são aplicadas automaticamente na inicialização, nesta ordem:

```
V1__create_usuario.sql
V2__create_categoria.sql
V3__create_produto.sql
V4__create_variacao_produto.sql
V5__create_fornecedor.sql
V6__create_estoque.sql
V7__create_movimentacao.sql
V8__create_pedido_compra.sql
V9__create_item_pedido_compra.sql
```

> Nunca altere uma migration já aplicada. Crie sempre uma nova versão (`V10__...`).

---

## Estrutura do projeto

```
src/
└── main/
    ├── java/com/example/Stq/
    │   ├── autenticacao/       # Login, JWT, refresh token
    │   ├── produto/            # Produto, Categoria, VariacaoProduto
    │   ├── fornecedor/         # Fornecedor + validação CNPJ
    │   ├── movimentacao/       # Movimentação, Estoque
    │   ├── pedidocompra/       # PedidoCompra, ItemPedidoCompra
    │   ├── relatorio/          # Dashboard, alertas, CSV
    │   ├── frontend/           # Controllers Thymeleaf + forms
    │   └── config/             # Security, JWT, OpenAPI
    └── resources/
        ├── db/migration/       # Scripts Flyway
        ├── templates/          # Templates Thymeleaf
        └── static/             # CSS e assets estáticos
```

---

## Testes

```bash
# Rodar todos os testes
./mvnw test

# Rodar com relatório de cobertura JaCoCo
./mvnw verify
```

Os testes seguem três níveis:

| Nível        | Anotação                           | Contexto                    |
|--------------|------------------------------------|-----------------------------|
| Unitário     | `@ExtendWith(MockitoExtension.class)` | Sem contexto Spring      |
| Controller   | `@WebMvcTest`                      | Slice test com MockMvc      |
| Integração   | `@SpringBootTest + @AutoConfigureMockMvc` | Banco real         |
