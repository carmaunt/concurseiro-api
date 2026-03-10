# Questões API — Endpoints de Questões

Este documento descreve os endpoints do módulo de **questões** da Concurseiro API.

Esse módulo é o núcleo funcional do sistema e concentra o cadastro, consulta e listagem paginada de questões de concursos públicos. As operações administrativas de atualização, exclusão e consulta de gabarito ficam em um módulo separado, documentado em `docs/endpoints/admin-questoes.md`.

---

# Base paths

O domínio de questões está dividido em dois grupos de rotas:

## Rotas principais

```http
/api/v1/questoes
```

## Rotas administrativas

```http
/api/v1/admin/questoes
```

---

# Visão geral

As rotas públicas/autenticadas de questões permitem:

* cadastrar uma nova questão
* buscar uma questão pelo identificador `idQuestion`
* listar questões com filtros e paginação

As rotas administrativas permitem:

* consultar o gabarito de uma questão
* atualizar uma questão existente
* excluir uma questão

As rotas abaixo foram confirmadas diretamente no código dos controllers `QuestaoController` e `AdminQuestaoController`. ([raw.githubusercontent.com](https://raw.githubusercontent.com/carmaunt/concurseiro-api/main/src/main/java/br/com/concurseiro/api/questoes/controller/QuestaoController.java))

---

# Modelo de identificação

A API usa o campo `idQuestion` como identificador nas rotas de detalhe, atualização, exclusão e gabarito.

Exemplo:

```http
GET /api/v1/questoes/Q123
```

Isso foi confirmado nos parâmetros `@PathVariable String idQuestion` dos controllers. ([raw.githubusercontent.com](https://raw.githubusercontent.com/carmaunt/concurseiro-api/main/src/main/java/br/com/concurseiro/api/questoes/controller/QuestaoController.java))

---

# POST /api/v1/questoes

Cadastra uma nova questão.

## Descrição

Recebe um `QuestaoRequest`, valida o corpo da requisição e retorna a questão cadastrada como `QuestaoResponse`. O endpoint responde com status **201 Created**. ([raw.githubusercontent.com](https://raw.githubusercontent.com/carmaunt/concurseiro-api/main/src/main/java/br/com/concurseiro/api/questoes/controller/QuestaoController.java))

## Request

```json
{
  "texto": "Enunciado da questão",
  "disciplinaId": 1,
  "assuntoId": 2,
  "bancaId": 3,
  "instituicaoId": 4,
  "ano": 2024,
  "cargo": "Analista",
  "nivel": "SUPERIOR",
  "modalidade": "MULTIPLA_ESCOLHA"
}
```

## Observações

* o payload exato depende da estrutura de `QuestaoRequest`
* o controller valida o corpo com `@Valid`
* a resposta é convertida por `QuestaoResponse.fromEntity(...)` ([raw.githubusercontent.com](https://raw.githubusercontent.com/carmaunt/concurseiro-api/main/src/main/java/br/com/concurseiro/api/questoes/controller/QuestaoController.java))

## Respostas

### 201 — Created

Questão cadastrada com sucesso. ([raw.githubusercontent.com](https://raw.githubusercontent.com/carmaunt/concurseiro-api/main/src/main/java/br/com/concurseiro/api/questoes/controller/QuestaoController.java))

### 400 — Bad Request

Dados inválidos enviados na requisição.

### 401 — Unauthorized

Requisição sem autenticação válida, caso a segurança da rota exija token.

### 500 — Internal Server Error

Erro inesperado no servidor.

---

# GET /api/v1/questoes/{idQuestion}

Busca uma questão pelo identificador.

## Descrição

Retorna uma questão específica a partir do `idQuestion`. A resposta é serializada como `QuestaoResponse`. ([raw.githubusercontent.com](https://raw.githubusercontent.com/carmaunt/concurseiro-api/main/src/main/java/br/com/concurseiro/api/questoes/controller/QuestaoController.java))

## Parâmetros de caminho

| Parâmetro  | Tipo   | Descrição                |
| ---------- | ------ | ------------------------ |
| idQuestion | string | identificador da questão |

## Exemplo

```http
GET /api/v1/questoes/Q123
```

## Resposta

```json
{
  "idQuestion": "Q123",
  "texto": "Enunciado da questão",
  "ano": 2024,
  "cargo": "Analista"
}
```

## Respostas

### 200 — OK

Questão encontrada. ([raw.githubusercontent.com](https://raw.githubusercontent.com/carmaunt/concurseiro-api/main/src/main/java/br/com/concurseiro/api/questoes/controller/QuestaoController.java))

### 404 — Not Found

Questão não encontrada.

---

# GET /api/v1/questoes

Lista questões com filtros e paginação.

## Descrição

Esse endpoint suporta busca paginada e múltiplos filtros. O retorno é um `Page` do Spring Data mapeado para `QuestaoResponse`. O controller impõe validações explícitas para `page` e `size`, incluindo limite máximo de **50 itens por página**. ([raw.githubusercontent.com](https://raw.githubusercontent.com/carmaunt/concurseiro-api/main/src/main/java/br/com/concurseiro/api/questoes/controller/QuestaoController.java))

## Query parameters suportados

| Parâmetro     | Tipo   | Descrição                      |
| ------------- | ------ | ------------------------------ |
| texto         | string | filtra por texto da questão    |
| disciplina    | string | filtra por nome da disciplina  |
| disciplinaId  | number | filtra por id da disciplina    |
| assunto       | string | filtra por nome do assunto     |
| assuntoId     | number | filtra por id do assunto       |
| banca         | string | filtra por nome da banca       |
| bancaId       | number | filtra por id da banca         |
| instituicao   | string | filtra por nome da instituição |
| instituicaoId | number | filtra por id da instituição   |
| ano           | number | filtra por ano                 |
| cargo         | string | filtra por cargo               |
| nivel         | string | filtra por nível               |
| modalidade    | string | filtra por modalidade          |
| sort          | string | ordenação                      |
| page          | number | número da página, começa em 0  |
| size          | number | quantidade de itens por página |

Todos esses parâmetros foram confirmados na assinatura do método `listar(...)` em `QuestaoController`. ([raw.githubusercontent.com](https://raw.githubusercontent.com/carmaunt/concurseiro-api/main/src/main/java/br/com/concurseiro/api/questoes/controller/QuestaoController.java))

## Regras de paginação

| Regra       | Valor |
| ----------- | ----- |
| page mínimo | 0     |
| size mínimo | 1     |
| size máximo | 50    |
| page padrão | 0     |
| size padrão | 10    |

As mensagens de erro também estão definidas no controller:

* `page não pode ser negativa`
* `size deve ser maior que zero`
* `size máximo permitido é 50` ([raw.githubusercontent.com](https://raw.githubusercontent.com/carmaunt/concurseiro-api/main/src/main/java/br/com/concurseiro/api/questoes/controller/QuestaoController.java))

## Exemplos

### Listagem simples

```http
GET /api/v1/questoes?page=0&size=10
```

### Filtro por disciplina e ano

```http
GET /api/v1/questoes?disciplina=Direito%20Constitucional&ano=2024&page=0&size=10
```

### Filtro por banca e ordenação

```http
GET /api/v1/questoes?banca=FGV&sort=ano,desc&page=0&size=20
```

## Exemplo de resposta

```json
{
  "content": [
    {
      "idQuestion": "Q123",
      "texto": "Enunciado da questão",
      "ano": 2024,
      "cargo": "Analista"
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 120,
  "totalPages": 12
}
```

## Respostas

### 200 — OK

Lista paginada retornada com sucesso. ([raw.githubusercontent.com](https://raw.githubusercontent.com/carmaunt/concurseiro-api/main/src/main/java/br/com/concurseiro/api/questoes/controller/QuestaoController.java))

### 400 — Bad Request

Parâmetros de paginação inválidos. Exemplos confirmados no código: `page` negativa, `size` menor que 1 e `size` maior que 50. ([raw.githubusercontent.com](https://raw.githubusercontent.com/carmaunt/concurseiro-api/main/src/main/java/br/com/concurseiro/api/questoes/controller/QuestaoController.java))

---

# GET /api/v1/admin/questoes/{idQuestion}/gabarito

Consulta o gabarito de uma questão.

## Descrição

Retorna um objeto `GabaritoResponse` contendo `idQuestion` e `gabarito`. Essa rota fica no controller administrativo. ([raw.githubusercontent.com](https://raw.githubusercontent.com/carmaunt/concurseiro-api/main/src/main/java/br/com/concurseiro/api/admin/AdminQuestaoController.java))

## Exemplo

```http
GET /api/v1/admin/questoes/Q123/gabarito
```

## Resposta

```json
{
  "idQuestion": "Q123",
  "gabarito": "A"
}
```

## Respostas

### 200 — OK

Gabarito retornado com sucesso. ([raw.githubusercontent.com](https://raw.githubusercontent.com/carmaunt/concurseiro-api/main/src/main/java/br/com/concurseiro/api/admin/AdminQuestaoController.java))

### 404 — Not Found

Questão não encontrada.

---

# PUT /api/v1/admin/questoes/{idQuestion}

Atualiza uma questão existente.

## Descrição

Recebe um `QuestaoRequest`, valida o corpo da requisição e retorna o recurso atualizado como `QuestaoResponse`. ([raw.githubusercontent.com](https://raw.githubusercontent.com/carmaunt/concurseiro-api/main/src/main/java/br/com/concurseiro/api/admin/AdminQuestaoController.java))

## Request

```json
{
  "texto": "Novo enunciado da questão",
  "disciplinaId": 1,
  "assuntoId": 2,
  "bancaId": 3,
  "instituicaoId": 4,
  "ano": 2024,
  "cargo": "Analista"
}
```

## Respostas

### 200 — OK

Questão atualizada com sucesso. ([raw.githubusercontent.com](https://raw.githubusercontent.com/carmaunt/concurseiro-api/main/src/main/java/br/com/concurseiro/api/admin/AdminQuestaoController.java))

### 400 — Bad Request

Dados inválidos.

### 404 — Not Found

Questão não encontrada.

---

# DELETE /api/v1/admin/questoes/{idQuestion}

Exclui uma questão.

## Descrição

Remove a questão identificada por `idQuestion`. O endpoint retorna **204 No Content** quando a exclusão é realizada com sucesso. ([raw.githubusercontent.com](https://raw.githubusercontent.com/carmaunt/concurseiro-api/main/src/main/java/br/com/concurseiro/api/admin/AdminQuestaoController.java))

## Exemplo

```http
DELETE /api/v1/admin/questoes/Q123
```

## Respostas

### 204 — No Content

Questão removida com sucesso. ([raw.githubusercontent.com](https://raw.githubusercontent.com/carmaunt/concurseiro-api/main/src/main/java/br/com/concurseiro/api/admin/AdminQuestaoController.java))

### 404 — Not Found

Questão não encontrada.

---

# Segurança e permissões

O código dos controllers de questões não explicita anotações de autorização por método, mas as rotas administrativas estão separadas sob `/api/v1/admin/questoes`, o que sugere restrição por configuração de segurança da aplicação. Isso precisa ser confirmado no documento de segurança ou na configuração do Spring Security antes de afirmar regras mais específicas. ([raw.githubusercontent.com](https://raw.githubusercontent.com/carmaunt/concurseiro-api/main/src/main/java/br/com/concurseiro/api/admin/AdminQuestaoController.java))

---

# Relação com outros documentos

Para complementar este módulo, consulte também:

```text
docs/paginacao.md
docs/erros.md
docs/autenticacao.md
```

Também é recomendável criar um documento separado para o modelo de dados de questão:

```text
docs/modelos/questao.md
```

---

# Observação importante

Os nomes exatos dos campos de `QuestaoRequest` e `QuestaoResponse` não aparecem nos trechos de controller consultados. Por isso, os exemplos de payload acima servem como modelo editorial da documentação e devem ser refinados quando os DTOs forem documentados diretamente a partir do código.
