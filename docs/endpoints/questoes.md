# Questões API — Endpoints de Questões

Este documento descreve os endpoints do módulo de **questões** da Concurseiro API.

O módulo concentra o cadastro, a consulta, a busca paginada e as operações administrativas sobre questões de concursos.

---

# Base paths

As rotas do módulo estão divididas em dois grupos:

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

As rotas principais permitem:

* cadastrar uma nova questão
* buscar uma questão pelo identificador `idQuestion`
* listar questões com filtros e paginação

As rotas administrativas permitem:

* consultar o gabarito de uma questão
* atualizar uma questão existente
* excluir uma questão

As rotas públicas e os contratos do módulo estão definidos em `QuestaoController`, `AdminQuestaoController`, `QuestaoRequest`, `QuestaoResponse`, `QuestaoService`, `QuestaoRepository` e `QuestaoSpecifications`. fileciteturn24file0 fileciteturn27file0 fileciteturn28file0 fileciteturn19file0 fileciteturn29file0 fileciteturn32file0 fileciteturn30file0

---

# Modelo de identificação

A API usa o campo `idQuestion` como identificador externo das questões. Esse campo é persistido como único na coluna `id_question` e possui tamanho máximo de 16 caracteres. fileciteturn31file0

Exemplo:

```http
GET /api/v1/questoes/Q123ABC456XYZ789
```

---

# Estrutura de dados retornada

As respostas de questão usam o DTO `QuestaoResponse`, com os seguintes campos: `idQuestion`, `enunciado`, `questao`, `alternativas`, `disciplina`, `disciplinaId`, `assunto`, `assuntoId`, `banca`, `bancaId`, `instituicao`, `instituicaoId`, `ano`, `cargo`, `nivel`, `modalidade`, `gabarito`, `provaId` e `criadoEm`. fileciteturn19file0

Exemplo de payload de resposta:

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

# POST /api/v1/questoes

Cadastra uma nova questão.

## Descrição

Recebe um `QuestaoRequest`, valida o corpo da requisição e retorna a questão cadastrada como `QuestaoResponse`. O endpoint responde com **201 Created**. fileciteturn24file0

## Body

Campos exigidos pelo request:

| Campo         | Tipo    | Obrigatório | Regras |
| ------------- | ------- | ----------- | ------ |
| enunciado     | string  | sim         | `@NotBlank`, máximo 10000 |
| questao       | string  | sim         | `@NotBlank`, máximo 10000 |
| alternativas  | string  | sim         | `@NotBlank`, máximo 10000 |
| disciplinaId  | number  | sim         | `@NotNull` |
| assuntoId     | number  | sim         | `@NotNull` |
| bancaId       | number  | sim         | `@NotNull` |
| instituicaoId | number  | sim         | `@NotNull` |
| ano           | number  | sim         | entre 1900 e 2100 |
| cargo         | string  | sim         | `@NotBlank`, máximo 160 |
| nivel         | string  | sim         | `@NotBlank`, máximo 80 |
| modalidade    | string  | sim         | `A_E`, `A_D` ou `CERTO_ERRADO` |
| gabarito      | string  | sim         | `@NotBlank` |

Essas validações estão definidas em `QuestaoRequest`. fileciteturn28file0

## Exemplo

```json
{
  "enunciado": "Leia o enunciado com atenção.",
  "questao": "Quanto é 2 + 2?",
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

## Regras de negócio

No cadastro, o serviço:

1. normaliza a modalidade com `QuestaoValidationHelper.normalizarModalidade(...)`
2. valida o gabarito de acordo com a modalidade
3. normaliza o gabarito
4. busca disciplina, assunto, banca e instituição no catálogo pelos IDs informados
5. gera um novo `idQuestion`
6. persiste a entidade `Questao` com os relacionamentos de catálogo preenchidos

Se algum item de catálogo não existir, o serviço retorna `404 Not Found` com mensagem específica. fileciteturn29file0

## Respostas

### 201 — Created

Questão cadastrada com sucesso. fileciteturn24file0

### 400 — Bad Request

Corpo inválido, modalidade inválida, gabarito incompatível com a modalidade ou parâmetros malformados.

### 401 — Unauthorized

Requisição sem autenticação válida.

### 403 — Forbidden

Usuário autenticado sem permissão para executar a operação.

### 404 — Not Found

Disciplina, assunto, banca ou instituição não encontrados no catálogo. fileciteturn29file0

---

# GET /api/v1/questoes/{idQuestion}

Busca uma questão pelo identificador.

## Descrição

Retorna uma questão específica a partir do `idQuestion`. O controller delega a busca ao serviço, que usa `findDetalhadaByIdQuestion(...)` para carregar a questão junto com disciplina, assunto, banca e instituição. fileciteturn24file0 fileciteturn29file0 fileciteturn32file0

## Parâmetros de caminho

| Parâmetro  | Tipo   | Descrição |
| ---------- | ------ | --------- |
| idQuestion | string | identificador da questão |

## Exemplo

```http
GET /api/v1/questoes/Q123ABC456XYZ789
```

## Respostas

### 200 — OK

Questão encontrada. fileciteturn24file0

### 404 — Not Found

Questão não encontrada. fileciteturn29file0

---

# GET /api/v1/questoes

Lista questões com filtros e paginação.

## Descrição

Retorna uma página de `QuestaoResponse`. O endpoint aceita filtros opcionais, valida paginação e delega a montagem da consulta para `QuestaoSpecifications`. fileciteturn24file0 fileciteturn29file0 fileciteturn30file0

## Query parameters suportados

| Parâmetro     | Tipo   | Obrigatório | Descrição |
| ------------- | ------ | ----------- | --------- |
| texto         | string | não         | busca textual em `textoBusca` |
| disciplinaId  | number | não         | filtra por id da disciplina |
| assuntoId     | number | não         | filtra por id do assunto |
| bancaId       | number | não         | filtra por id da banca |
| instituicaoId | number | não         | filtra por id da instituição |
| ano           | number | não         | filtra por ano |
| cargo         | string | não         | filtra por cargo |
| nivel         | string | não         | filtra por nível |
| modalidade    | string | não         | filtra por modalidade |
| sort          | string | não         | ordenação |
| page          | number | não         | número da página |
| size          | number | não         | quantidade de itens por página |

Os parâmetros aceitos são exatamente os definidos no método `listar(...)`. Não existem filtros textuais por nome de disciplina, assunto, banca ou instituição nesse endpoint. fileciteturn24file0

## Regras de paginação

| Regra       | Valor |
| ----------- | ----- |
| page mínimo | 0 |
| size mínimo | 1 |
| size máximo | 50 |
| page padrão | 0 |
| size padrão | 10 |

As mensagens retornadas pelo controller para paginação inválida são:

* `page não pode ser negativa`
* `size deve ser maior que zero`
* `size máximo permitido é 50` fileciteturn24file0

## Regras de ordenação

O serviço só aceita ordenação pelos campos:

* `ano`
* `criadoEm`

Quando `sort` não é informado, a paginação é criada sem ordenação explícita. Quando informado, o formato esperado é `campo` ou `campo,direcao`, com `desc` para ordem decrescente. Qualquer outro campo gera `400 Bad Request` com a mensagem `sort inválido. Permitidos: ano, criadoEm` ou equivalente conforme a montagem do conjunto permitido. fileciteturn29file0

Exemplos válidos:

```http
GET /api/v1/questoes?sort=ano
```

```http
GET /api/v1/questoes?sort=criadoEm,desc
```

## Regras dos filtros

A filtragem é implementada por specifications com o seguinte comportamento:

* `texto` faz busca em `textoBusca`, removendo acentos, normalizando espaços e convertendo para maiúsculas antes do `LIKE`
* `disciplinaId`, `assuntoId`, `bancaId` e `instituicaoId` filtram pelos relacionamentos de catálogo
* `ano` usa igualdade numérica
* `cargo` e `nivel` usam comparação case-insensitive
* `modalidade` usa comparação case-insensitive com normalização para maiúsculas fileciteturn30file0

## Exemplos

### Listagem simples

```http
GET /api/v1/questoes?page=0&size=10
```

### Filtro por disciplina e ano

```http
GET /api/v1/questoes?disciplinaId=1&ano=2024&page=0&size=10
```

### Busca textual com ordenação

```http
GET /api/v1/questoes?texto=controle%20de%20constitucionalidade&sort=ano,desc&page=0&size=20
```

## Exemplo de resposta

```json
{
  "content": [
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
  ],
  "page": {
    "size": 10,
    "number": 0,
    "totalElements": 120,
    "totalPages": 12
  }
}
```

## Respostas

### 200 — OK

Lista paginada retornada com sucesso. fileciteturn24file0

### 400 — Bad Request

Parâmetros de paginação inválidos ou `sort` inválido. fileciteturn24file0 fileciteturn29file0

---

# GET /api/v1/admin/questoes/{idQuestion}/gabarito

Consulta o gabarito de uma questão.

## Descrição

Retorna um objeto com `idQuestion` e `gabarito`. O controller busca a questão pelo identificador e monta um `GabaritoResponse`. fileciteturn27file0

## Exemplo

```http
GET /api/v1/admin/questoes/Q123ABC456XYZ789/gabarito
```

## Exemplo de resposta

```json
{
  "idQuestion": "Q123ABC456XYZ789",
  "gabarito": "D"
}
```

## Respostas

### 200 — OK

Gabarito retornado com sucesso. fileciteturn27file0

### 404 — Not Found

Questão não encontrada. fileciteturn29file0

---

# PUT /api/v1/admin/questoes/{idQuestion}

Atualiza uma questão existente.

## Descrição

Recebe um `QuestaoRequest`, valida o corpo da requisição e retorna a questão atualizada como `QuestaoResponse`. fileciteturn27file0

## Body

O payload é o mesmo usado em `POST /api/v1/questoes` e exige todos os campos obrigatórios de `QuestaoRequest`. fileciteturn28file0

## Exemplo

```json
{
  "enunciado": "Novo enunciado da questão.",
  "questao": "Novo texto da questão.",
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

## Regras de negócio

Na atualização, o serviço:

1. localiza a questão por `idQuestion`
2. valida e normaliza modalidade e gabarito
3. busca novamente os itens de catálogo informados
4. sobrescreve os dados da questão existente
5. salva a entidade atualizada

Se a questão não existir, retorna `404 Not Found`. Se algum item de catálogo não existir, também retorna `404 Not Found`. fileciteturn29file0

## Respostas

### 200 — OK

Questão atualizada com sucesso. fileciteturn27file0

### 400 — Bad Request

Dados inválidos, modalidade inválida ou gabarito incompatível com a modalidade.

### 404 — Not Found

Questão não encontrada, ou catálogo referenciado inexistente. fileciteturn29file0

---

# DELETE /api/v1/admin/questoes/{idQuestion}

Exclui uma questão.

## Descrição

Remove a questão identificada por `idQuestion` e retorna **204 No Content** quando a exclusão é concluída. fileciteturn27file0

## Exemplo

```http
DELETE /api/v1/admin/questoes/Q123ABC456XYZ789
```

## Respostas

### 204 — No Content

Questão removida com sucesso. fileciteturn27file0

### 404 — Not Found

Questão não encontrada. fileciteturn29file0

---

# Regras do modelo de questão relevantes para integração

A entidade `Questao` possui:

* `idQuestion` único
* `enunciado`, `questao` e `alternativas` persistidos como `@Lob`
* `ano`, `cargo`, `nivel`, `modalidade`, `gabarito`
* `provaId`, quando a questão estiver vinculada a uma prova
* `criadoEm`
* relacionamentos obrigatórios com `Disciplina`, `Assunto`, `Banca` e `Instituicao`
* campo `textoBusca`, preenchido automaticamente em `@PrePersist` e `@PreUpdate` para otimizar busca textual fileciteturn31file0

O campo `textoBusca` é gerado a partir de `enunciado`, `questao` e nome do assunto, com remoção de acentos, normalização de espaços e conversão para maiúsculas. fileciteturn31file0

---

# Segurança e permissões

A documentação OpenAPI do projeto declara autenticação por bearer token JWT no esquema global de segurança. Os endpoints de questões e de administração de questões estão presentes na especificação publicada do sistema. fileciteturn23file0

No OpenAPI atual:

* `POST /api/v1/questoes` está documentado como endpoint autenticado
* `GET /api/v1/questoes`
* `GET /api/v1/questoes/{idQuestion}`
* `GET /api/v1/admin/questoes/{idQuestion}/gabarito`
* `PUT /api/v1/admin/questoes/{idQuestion}`
* `DELETE /api/v1/admin/questoes/{idQuestion}`

estão presentes com seus contratos de request e response. fileciteturn23file0

---

# Arquivos principais do módulo

```text
src/main/java/br/com/concurseiro/api/questoes/controller/QuestaoController.java
src/main/java/br/com/concurseiro/api/admin/AdminQuestaoController.java
src/main/java/br/com/concurseiro/api/questoes/dto/QuestaoRequest.java
src/main/java/br/com/concurseiro/api/questoes/dto/QuestaoResponse.java
src/main/java/br/com/concurseiro/api/questoes/service/QuestaoService.java
src/main/java/br/com/concurseiro/api/questoes/repository/QuestaoRepository.java
src/main/java/br/com/concurseiro/api/questoes/spec/QuestaoSpecifications.java
src/main/java/br/com/concurseiro/api/questoes/model/Questao.java
```
