# Concurseiro API

> API REST para gerenciamento de questões de concursos públicos, provas, usuários e comentários.

---

## Quick Start

```bash
# Compilar
./mvnw clean package -DskipTests

# Executar
./mvnw spring-boot:run
```

A API estará disponível em `https://concurseiro-api-lnae.onrender.com`

---

## Autenticação

A API utiliza **JWT** para autenticação. Após login, envie o token no header:

```
Authorization: Bearer <token>
```

### Registro de Usuário

```http
POST /api/v1/auth/register
Content-Type: application/json

{
  "nome": "João Silva",
  "email": "joao@email.com",
  "senha": "SenhaForte123!"
}
```

**Regras de senha:**
- Mínimo 8 caracteres
- Pelo menos uma letra minúscula
- Pelo menos uma letra maiúscula
- Pelo menos um número
- Pelo menos um símbolo

**Status inicial:** Usuários criados ficam com status `PENDENTE`. Um ADMIN deve ativá-los.

### Login

```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "email": "joao@email.com",
  "senha": "SenhaForte123!"
}
```

**Resposta:**

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "dGhpcyBpcyBhIHJlZnJlc2ggdG9rZW4...",
  "email": "joao@email.com",
  "role": "VISITANTE"
}
```

### Refresh Token

```http
POST /api/v1/auth/refresh
Content-Type: application/json

{
  "refreshToken": "dGhpcyBpcyBhIHJlZnJlc2ggdG9rZW4..."
}
```

---

## Endpoints

### Legenda

| Símbolo | Significado |
|---------|-------------|
| 🔒 | Requer autenticação |
| ⚙️ | Requer role ADMIN |

---

### Auth (`/api/v1/auth`)

| Método | Endpoint | Descrição |
|--------|----------|------------|
| POST | `/register` | Criar novo usuário |
| POST | `/login` | Autenticar |
| POST | `/refresh` | Renovar token |

---

### Questões (`/api/v1/questoes`)

| Método | Endpoint | Descrição |
|--------|----------|------------|
| 🔒 POST | `/` | Cadastrar questão |
| 🔒 GET | `/{idQuestion}` | Buscar por ID |
| 🔒 GET | `/` | Listar com filtros |

**Filtros de listagem:**

| Parâmetro | Tipo | Descrição |
|-----------|------|------------|
| `texto` | string | Busca em enunciado/questão |
| `disciplinaId` | long | Filtrar por disciplina |
| `assuntoId` | long | Filtrar por assunto |
| `bancaId` | long | Filtrar por banca |
| `instituicaoId` | long | Filtrar por instituição |
| `ano` | int | Filtrar por ano |
| `cargo` | string | Filtrar por cargo |
| `nivel` | string | Filtrar por nível |
| `modalidade` | string | `A_E`, `A_D`, `CERTO_ERRADO` |
| `page` | int | Página (padrão: 0) |
| `size` | int | Tamanho (máx: 50) |

**Criar questão:**

```http
POST /api/v1/questoes
Content-Type: application/json
Authorization: Bearer <token>

{
  "enunciado": "Analise o seguinte...",
  "questao": "Qual o resultado?",
  "alternativas": "A) 1\nB) 2\nC) 3\nD) 4\nE) 5",
  "disciplinaId": 1,
  "assuntoId": 10,
  "bancaId": 3,
  "instituicaoId": 7,
  "ano": 2024,
  "cargo": "Analista Judiciário",
  "nivel": "SUPERIOR",
  "modalidade": "A_E",
  "gabarito": "D"
}
```

**Resposta:**

```json
{
  "idQuestion": "Q123ABC456XYZ789",
  "enunciado": "Analise o seguinte...",
  "questao": "Qual o resultado?",
  "alternativas": "A) 1\nB) 2\nC) 3\nD) 4\nE) 5",
  "disciplina": "Matemática",
  "disciplinaId": 1,
  "assunto": "Aritmética",
  "assuntoId": 10,
  "banca": "FGV",
  "bancaId": 3,
  "instituicao": "TRF 1",
  "instituicaoId": 7,
  "ano": 2024,
  "cargo": "Analista Judiciário",
  "nivel": "SUPERIOR",
  "modalidade": "A_E",
  "gabarito": "D",
  "provaId": null,
  "criadoEm": "2026-04-27T10:00:00Z"
}
```

---

### Admin - Questões (`/api/v1/admin/questoes`)

| Método | Endpoint | Descrição |
|--------|----------|------------|
| ⚙️ 🔒 GET | `/{idQuestion}/gabarito` | Ver gabarito |
| ⚙️ 🔒 PUT | `/{idQuestion}` | Atualizar |
| ⚙️ 🔒 DELETE | `/{idQuestion}` | Excluir |

---

### Provas (`/api/v1/provas`)

| Método | Endpoint | Descrição |
|--------|----------|------------|
| 🔒 POST | `/` | Criar prova |
| 🔒 GET | `/{id}` | Buscar por ID |
| 🔒 GET | `/` | Listar (paginado) |
| 🔒 POST | `/{provaId}/questoes` | Adicionar questão |
| ⚙️ 🔒 DELETE | `/{id}` | Excluir prova |

**Criar prova:**

```http
POST /api/v1/provas
Content-Type: application/json
Authorization: Bearer <token>

{
  "banca": "FGV",
  "instituicaoId": 7,
  "ano": 2024,
  "cargo": "Analista Judiciário",
  "nivel": "SUPERIOR",
  "modalidade": "MÚLTIPLA ESCOLHA A-E"
}
```

**Modalidades aceitas:**
- `MÚLTIPLA ESCOLHA A-E` / `A_E`
- `MÚLTIPLA ESCOLHA A-D` / `A_D`
- `CERTO E ERRADO` / `CERTO_ERRADO`

**Resposta:**

```json
{
  "id": 1,
  "banca": "FGV",
  "instituicao": "TRF 1",
  "instituicaoId": 7,
  "ano": 2024,
  "cargo": "Analista Judiciário",
  "nivel": "SUPERIOR",
  "modalidade": "MÚLTIPLA ESCOLHA A-E",
  "totalQuestoes": 0,
  "criadoEm": "2026-04-27T10:00:00Z"
}
```

**Adicionar questão à prova:**

```http
POST /api/v1/provas/1/questoes
Content-Type: application/json
Authorization: Bearer <token>

{
  "enunciado": "...",
  "questao": "...",
  "alternativas": "...",
  "disciplinaId": 1,
  "assuntoId": 10,
  "bancaId": 3,
  "instituicaoId": 7,
  "ano": 2024,
  "cargo": "Analista",
  "nivel": "SUPERIOR",
  "modalidade": "A_E",
  "gabarito": "D"
}
```

---

### Comentários (`/api/v1/questoes/{questaoId}/comentarios`)

| Método | Endpoint | Descrição |
|--------|----------|------------|
| 🔒 GET | `/` | Listar comentários |
| 🔒 POST | `/` | Adicionar comentário |
| 🔒 POST | `/{id}/curtir` | Curtir |
| 🔒 POST | `/{id}/descurtir` | Descurtir |

**Parâmetros de ordenação:**
- `ordenar=recentes` (padrão)
- `ordenar=curtidas`

**Criar comentário:**

```http
POST /api/v1/questoes/Q123ABC/comentarios
Content-Type: application/json
Authorization: Bearer <token>

{
  "texto": "Ótima questão! A resposta é realmente D."
}
```

**Resposta:**

```json
{
  "id": 1,
  "questaoId": "Q123ABC",
  "autor": "joao@email.com",
  "texto": "Ótima questão! A resposta é realmente D.",
  "curtidas": 0,
  "descurtidas": 0,
  "criadoEm": "2026-04-27T10:00:00Z"
}
```

---

### Admin - Usuários (`/api/v1/admin/usuarios`)

| Método | Endpoint | Descrição |
|--------|----------|------------|
| ⚙️ 🔒 GET | `/` | Listar usuários |
| ⚙️ 🔒 PATCH | `/{id}/ativar` | Ativar usuário |
| ⚙️ 🔒 DELETE | `/{id}` | Excluir visitante |

**Resposta de listagem:**

```json
{
  "content": [
    {
      "id": 1,
      "nome": "João Silva",
      "email": "joao@email.com",
      "role": "VISITANTE",
      "status": "PENDENTE"
    }
  ],
  "totalElements": 1,
  "totalPages": 1
}
```

---

### Catálogo - Disciplinas (`/api/v1/catalogo/disciplinas`)

| Método | Endpoint | Descrição |
|--------|----------|------------|
| GET | `/` | Listar todas |

**Resposta:**

```json
[
  { "id": 1, "nome": "Matemática" },
  { "id": 2, "nome": "Português" }
]
```

---

### Catálogo - Assuntos (`/api/v1/catalogo/disciplinas/{disciplinaId}/assuntos`)

| Método | Endpoint | Descrição |
|--------|----------|------------|
| GET | `/` | Listar assuntos de uma disciplina |

---

### Catálogo - Subassuntos (`/api/v1/catalogo/assuntos/{assuntoId}/subassuntos`)

| Método | Endpoint | Descrição |
|--------|----------|------------|
| GET | `/` | Listar subassuntos de um assunto |

---

### Catálogo - Bancas (`/api/v1/catalogo/bancas`)

| Método | Endpoint | Descrição |
|--------|----------|------------|
| GET | `/` | Listar todas as bancas |

---

### Catálogo - Instituições (`/api/v1/catalogo/instituicoes`)

| Método | Endpoint | Descrição |
|--------|----------|------------|
| GET | `/` | Listar todas as instituições |

---

### Admin - Catálogo

| Recurso | Endpoint | Métodos |
|---------|----------|---------|
| Disciplinas | `/api/v1/admin/catalogo/disciplinas` | POST, PUT, DELETE |
| Assuntos | `/api/v1/admin/catalogo/assuntos` | POST |
| Subassuntos | `/api/v1/admin/catalogo/subassuntos` | POST |
| Bancas | `/api/v1/admin/catalogo/bancas` | POST, PUT, DELETE |
| Instituições | `/api/v1/admin/catalogo/instituicoes` | GET, POST, PUT, DELETE |

**Criar disciplina:**

```http
POST /api/v1/admin/catalogo/disciplinas
Content-Type: application/json
Authorization: Bearer <token>

{ "nome": "Matemática" }
```

---

## Documentação Interativa

Acesse a documentação Swagger em:

- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON**: `http://localhost:8080/v3/api-docs`

---

## Variáveis de Ambiente

| Variável | Padrão | Descrição |
|----------|--------|------------|
| `PGHOST` | localhost | Host do PostgreSQL |
| `PGPORT` | 5432 | Porta do PostgreSQL |
| `PGDATABASE` | concurseiro | Nome do banco |
| `PGUSER` | postgres | Usuário do banco |
| `PGPASSWORD` | - | Senha do banco |
| `JWT_SECRET` | - | Chave secreta JWT |
| `JWT_EXPIRATION_MS` | 14400000 | Expiração token (4h) |
| `CORS_ALLOWED_ORIGINS` | `*` | Origins permitidas |
| `FIREBASE_CREDENTIALS_PATH` | - | Path credentials Firebase |
| `APP_ADMIN_API_KEY` | admin-dev-key | Chave API admin |

---

## Docker

```bash
# Build
docker build -t concurseiro-api .

# Run
docker run -p 8080:8080 \
  -e PGHOST=host.docker.internal \
  -e PGPASSWORD=senha \
  -e JWT_SECRET=sua-chave-secreta \
  concurseiro-api
```

---

## Stack

- **Java 21**
- **Spring Boot 4.0.2**
- **Spring Security**
- **Spring Data JPA**
- **PostgreSQL**
- **JWT (JJWT 0.12.6)**
- **Liquibase 4.24.0**
- **Springdoc OpenAPI 3.0.1**
- **Micrometer / Prometheus**

---

## Estrutura do Projeto

```
src/main/java/br/com/concurseiro/api/
├── auth/                    # Autenticação
├── usuarios/               # Usuários
│   ├── controller/
│   ├── dto/
│   ├── model/
│   ├── repository/
│   ├── service/
│   └── token/              # Refresh tokens
├── questoes/               # Questões
│   ├── controller/
│   ├── dto/
│   ├── model/
│   ├── repository/
│   └── service/
├── provas/                 # Provas
├── comentario/             # Comentários
├── catalogo/               # Catálogos
│   ├── disciplina/
│   ├── assunto/
│   ├── subassunto/
│   ├── banca/
│   └── instituicao/
├── admin/                  # Endpoints admin
└── infra/
    ├── security/           # JWT, Firebase
    └── observability/      # Métricas
```

---

## Roles e Permissions

| Role | Descrição |
|------|------------|
| `VISITANTE` | Usuário comum |
| `ADMIN` | Administrador |

| Status | Descrição |
|--------|------------|
| `PENDENTE` | Aguardando aprovação |
| `ATIVO` | Aprovado para login |

| AuthProvider | Descrição |
|--------------|------------|
| `LOCAL` | Login com senha |
| `GOOGLE` | Login via Firebase |

---

## Arquitetura

A API segue uma arquitetura baseada em camadas:

```
Controller → Service → Repository → Banco de dados
```

### Camadas

- **Controller**: Expor endpoints HTTP, validar dados de entrada, chamar serviços
- **Service**: Regras de negócio, validações, orquestração de acesso a dados
- **Repository**: Acesso ao banco de dados via Spring Data JPA

---

## Configuração

### Variáveis de Ambiente

| Variável | Padrão | Descrição |
|----------|--------|------------|
| `PGHOST` | localhost | Host do PostgreSQL |
| `PGPORT` | 5432 | Porta do PostgreSQL |
| `PGDATABASE` | concurseiro | Nome do banco |
| `PGUSER` | postgres | Usuário do banco |
| `PGPASSWORD` | - | Senha do banco |
| `JWT_SECRET` | - | Chave secreta para JWT |
| `JWT_EXPIRATION_MS` | 14400000 | Expiração do token (4h) |
| `APP_ADMIN_API_KEY` | admin-dev-key | Chave de API administrativa |

### Banco de Dados

- **PostgreSQL** com Liquibase para migrações
- **HikariCP** para connection pooling (min: 2, max: 10)
- **Hibernate** em modo `validate` (não altera schema)

---

## Autenticação

A API utiliza **JWT (JSON Web Token)** para autenticação stateless.

### Fluxo de Autenticação

1. Usuário realiza cadastro (`POST /api/v1/auth/register`)
2. Usuário é criado com status **PENDENTE** e role **VISITANTE**
3. Administrador aprova o usuário (status → **ATIVO**)
4. Usuário realiza login (`POST /api/v1/auth/login`)
5. API retorna token JWT
6. Token deve ser enviado no header `Authorization: Bearer <token>`

### Roles

| Role | Descrição |
|------|------------|
| `VISITANTE` | Usuário comum |
| `ADMIN` | Administrador do sistema |

### Status

| Status | Descrição |
|--------|------------|
| `PENDENTE` | Aguardando aprovação |
| `ATIVO` | Aprovado e pode autenticar |

---

## Endpoints

### Base Path

```
/api/v1
```

### Módulos

| Módulo | Path | Descrição |
|--------|------|------------|
| Auth | `/api/v1/auth` | Registro e login |
| Questões | `/api/v1/questoes` | Cadastro e consulta de questões |
| Provas | `/api/v1/provas` | Gerenciamento de provas |
| Usuários (Admin) | `/api/v1/admin/usuarios` | Administração de usuários |
| Questões (Admin) | `/api/v1/admin/questoes` | Administração de questões |

---

### Auth

| Método | Endpoint | Descrição |
|--------|----------|------------|
| POST | `/api/v1/auth/register` | Registrar novo usuário |
| POST | `/api/v1/auth/login` | Autenticar usuário |

#### Registro

```json
POST /api/v1/auth/register
{
  "nome": "Maria da Silva",
  "email": "maria@email.com",
  "senha": "123456"
}
```

#### Login

```json
POST /api/v1/auth/login
{
  "email": "maria@email.com",
  "senha": "123456"
}
```

Resposta:

```json
{
  "success": true,
  "data": {
    "token": "jwt_token",
    "email": "maria@email.com",
    "role": "VISITANTE"
  }
}
```

---

### Questões

| Método | Endpoint | Descrição | Autenticado |
|--------|----------|------------|-------------|
| POST | `/api/v1/questoes` | Cadastrar questão | Sim |
| GET | `/api/v1/questoes/{idQuestion}` | Buscar questão por ID | Sim |
| GET | `/api/v1/questoes` | Listar questões (paginado) | Sim |
| GET | `/api/v1/admin/questoes/{idQuestion}/gabarito` | Consultar gabarito | Sim (Admin) |
| PUT | `/api/v1/admin/questoes/{idQuestion}` | Atualizar questão | Sim (Admin) |
| DELETE | `/api/v1/admin/questoes/{idQuestion}` | Excluir questão | Sim (Admin) |

#### Estrutura de Questão

```json
{
  "idQuestion": "Q123ABC456XYZ789",
  "enunciado": "Leia o enunciado com atenção.",
  "questao": "Quanto é 2 + 2?",
  "alternativas": "A) 1\nB) 2\nC) 3\nD) 4\nE) 5",
  "disciplina": "Matemática",
  "disciplinaId": 1,
  "assunto": "Aritmética",
  "assuntoId": 10,
  "banca": "FGV",
  "bancaId": 3,
  "instituicao": "TRF 1",
  "instituicaoId": 7,
  "ano": 2024,
  "cargo": "Analista Judiciário",
  "nivel": "SUPERIOR",
  "modalidade": "A_E",
  "gabarito": "D",
  "provaId": null,
  "criadoEm": "2026-03-09T10:15:30Z"
}
```

---

### Provas

| Método | Endpoint | Descrição | Autenticado |
|--------|----------|------------|-------------|
| POST | `/api/v1/provas` | Criar prova | Sim |
| GET | `/api/v1/provas/{id}` | Buscar prova por ID | Sim |
| GET | `/api/v1/provas` | Listar provas (paginado) | Sim |
| POST | `/api/v1/provas/{id}/questoes` | Adicionar questão à prova | Sim |

#### Estrutura de Prova

```json
{
  "id": 1,
  "banca": "FGV",
  "instituicao": "TRF 1",
  "ano": 2024,
  "cargo": "Analista Judiciário",
  "nivel": "SUPERIOR",
  "modalidade": "A_E",
  "totalQuestoes": 5,
  "criadoEm": "2026-03-09T10:15:30Z"
}
```

#### Restrição de Unicidade

Uma prova é única pela combinação:
- `banca` + `instituicao_id` + `ano` + `cargo` + `nivel` + `modalidade`

---

### Usuários (Admin)

| Método | Endpoint | Descrição | Autenticado |
|--------|----------|------------|-------------|
| GET | `/api/v1/admin/usuarios` | Listar usuários (paginado) | Sim (Admin) |
| PUT | `/api/v1/admin/usuarios/{id}/ativar` | Ativar usuário | Sim (Admin) |
| DELETE | `/api/v1/admin/usuarios/{id}` | Excluir usuário | Sim (Admin) |

#### Parâmetros de Paginação

| Parâmetro | Padrão | Máximo |
|-----------|--------|--------|
| `page` | 0 | - |
| `size` | 20 | 50 |

#### Estrutura de Usuário

```json
{
  "id": 5,
  "nome": "Maria Silva",
  "email": "maria@email.com",
  "role": "VISITANTE",
  "status": "ATIVO"
}
```

---

## Documentação Interativa

A API possui documentação OpenAPI disponível em:

- **Swagger UI**: `/swagger-ui.html`
- **OpenAPI JSON**: `/v3/api-docs`

---

## Executando a Aplicação

### Pré-requisitos

- Java 21
- Maven 3.9+
- PostgreSQL 14+

### Desenvolvimento

```bash
# Compilar
./mvnw clean package

# Executar
./mvnw spring-boot:run
```

### Docker

```bash
# Build da imagem
docker build -t concurseiro-api .

# Executar container
docker run -p 8080:8080 \
  -e PGHOST=host.docker.internal \
  -e PGPASSWORD=senha \
  -e JWT_SECRET=sua-chave-secreta \
  concurseiro-api
```

### Docker Compose

```yaml
version: '3.8'
services:
  api:
    build: .
    ports:
      - "8080:8080"
    environment:
      - PGHOST=db
      - PGPASSWORD=senha
      - JWT_SECRET=sua-chave-secreta
    depends_on:
      - db
  db:
    image: postgres:14
    environment:
      - POSTGRES_PASSWORD=senha
      - POSTGRES_DB=concurseiro
```

---

## Testes

O projeto utiliza Testcontainers para testes de integração com PostgreSQL real.

```bash
# Executar testes
./mvnw test
```

---

## Estrutura do Projeto

```
src/
├── main/
│   ├── java/
│   │   └── br/
│   │       └── com/
│   │           └── concurseiro/
│   │               └── api/
│   │                   ├── auth/           # Autenticação
│   │                   ├── usuarios/       # Usuários
│   │                   ├── questoes/       # Questões
│   │                   ├── provas/         # Provas
│   │                   ├── comentarios/    # Comentários
│   │                   ├── catalogo/       # Catálogos (disciplinas, bancas, etc)
│   │                   └── admin/          # Endpoints administrativos
│   └── resources/
│       ├── application.properties
│       └── db/
│           └── changelog/                  # Migrações Liquibase
└── test/
    └── java/
        └── br/
            └── com/
                └── concurseiro/
                    └── api/
```

---

## Banco de Dados

### Tabelas Principais

- `usuarios` - Cadastro de usuários
- `questoes` - Questões de concursos
- `provas` - Cabeçalhos de provas
- `prova_questoes` - Relação prova-questão
- `comentarios` - Comentários em questões
- `disciplinas` - Catálogo de disciplinas
- `assuntos` - Catálogo de assuntos
- `bancas` - Catálogo de bancas
- `instituicoes` - Catálogo de instituições
- `refresh_tokens` - Tokens de refresh

### Migrações

As migrações são gerenciadas via Liquibase em `src/main/resources/db/changelog/`.

---

## Licença

MIT