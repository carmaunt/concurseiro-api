# Provas API — Endpoints de Provas

Este documento descreve o estado atual do módulo de **provas** da Concurseiro API com base no código-fonte do repositório.

O módulo é responsável por:

- criar o cabeçalho de uma prova
- buscar uma prova por identificador
- listar provas com paginação
- cadastrar uma nova questão diretamente dentro de uma prova

---

# Base path

Todos os endpoints deste módulo utilizam o prefixo:

```http
/api/v1/provas
```

Origem no código:

```java
@RequestMapping("/api/v1/provas")
```

---

# Fonte da análise

Esta documentação foi atualizada a partir das classes e arquivos abaixo:

```text
src/main/java/br/com/concurseiro/api/prova/controller/ProvaController.java
src/main/java/br/com/concurseiro/api/prova/service/ProvaService.java
src/main/java/br/com/concurseiro/api/prova/model/Prova.java
src/main/java/br/com/concurseiro/api/prova/repository/ProvaRepository.java
src/main/java/br/com/concurseiro/api/prova/dto/ProvaRequest.java
src/main/java/br/com/concurseiro/api/prova/dto/ProvaResponse.java
src/main/java/br/com/concurseiro/api/prova/dto/ProvaQuestaoRequest.java
src/main/java/br/com/concurseiro/api/questoes/dto/QuestaoResponse.java
openapi.json
README.md
```

---

# Visão geral do módulo

## Entidade `Prova`

A prova é persistida na tabela `provas` e possui os seguintes campos relevantes:

| Campo | Tipo | Obrigatório | Observações |
| --- | --- | --- | --- |
| id | Long | sim | chave primária autogerada |
| banca | String | sim | até 160 caracteres |
| instituicaoCatalogo | Instituicao | sim | relacionamento `@ManyToOne` |
| ano | Integer | sim | ano da prova |
| cargo | String | sim | até 160 caracteres |
| nivel | String | sim | até 80 caracteres |
| modalidade | String | sim | até 40 caracteres |
| criadoEm | OffsetDateTime | sim | preenchido automaticamente |

## Restrição de unicidade

Existe uma constraint única no banco para impedir duplicidade de cabeçalho de prova considerando:

```text
banca + instituicao_id + ano + cargo + nivel + modalidade
```

Isso significa que duas provas com esse mesmo conjunto de dados não podem coexistir.

## Ordenação da listagem

A listagem usa ordenação por `criadoEm` em ordem decrescente.

---

# Regras de negócio confirmadas no serviço

## Criação de prova

Ao criar uma prova, o serviço:

1. valida se a instituição informada existe no catálogo
2. normaliza os campos `banca`, `cargo` e `nivel`
3. normaliza a `modalidade`
4. verifica se já existe prova com o mesmo cabeçalho
5. salva a prova
6. retorna `ProvaResponse` com `totalQuestoes = 0`

## Busca por id

Ao buscar uma prova, o serviço:

1. procura a prova no banco
2. retorna 404 se não existir
3. conta quantas questões pertencem à prova com `countByProvaId(id)`
4. devolve `ProvaResponse`

## Listagem

Ao listar provas, o serviço:

1. aplica `page` e `size`
2. ordena por `criadoEm desc`
3. para cada item, calcula `totalQuestoes`

## Lançamento de questão em uma prova

Ao lançar uma questão em uma prova, o serviço:

1. valida se a prova existe
2. valida o `gabarito` conforme a modalidade da prova
3. carrega `Disciplina` e `Assunto` do catálogo por id
4. carrega `Instituicao` usando a instituição associada à prova
5. carrega `Banca` pelo nome da prova
6. cria uma nova entidade `Questao`
7. herda da prova os campos `ano`, `cargo`, `nivel`, `modalidade` e `instituicao`
8. define `provaId` na questão
9. salva a nova questão

Importante: esse endpoint **não vincula uma questão já existente**. Ele realmente **cria uma nova questão dentro da prova**.

---

# Modalidades aceitas

O request de criação de prova aceita estes formatos para `modalidade`:

```text
A_E
A_D
CERTO_ERRADO
MULTIPLA ESCOLHA
MÚLTIPLA ESCOLHA
MULTIPLA ESCOLHA A-E
MULTIPLA ESCOLHA A_D
MÚLTIPLA ESCOLHA A-E
MÚLTIPLA ESCOLHA A_D
CERTO E ERRADO
CERTO/ERRADO
```

Depois disso, o serviço normaliza o valor antes de persistir.

Na prática, a validação de gabarito no lançamento da questão depende da modalidade já normalizada da prova.

---

# POST /api/v1/provas

Cria uma nova prova.

## Descrição

Recebe um `ProvaRequest`, valida o corpo com `@Valid`, verifica duplicidade e persiste uma nova prova.

## Request body

```json
{
  "banca": "CESPE",
  "instituicaoId": 1,
  "ano": 2024,
  "cargo": "Analista Judiciário",
  "nivel": "SUPERIOR",
  "modalidade": "A_E"
}
```

## Campos do request

| Campo | Tipo | Obrigatório | Regras |
| --- | --- | --- | --- |
| banca | string | sim | `@NotBlank`, máximo 160 |
| instituicaoId | number | sim | `@NotNull` |
| ano | number | sim | entre 1900 e 2100 |
| cargo | string | sim | `@NotBlank`, máximo 160 |
| nivel | string | sim | `@NotBlank`, máximo 80 |
| modalidade | string | sim | `@NotBlank` + regex específica |

## Resposta de sucesso

### 201 — Created

```json
{
  "id": 1,
  "banca": "CESPE",
  "instituicao": "PC-BA",
  "instituicaoId": 1,
  "ano": 2024,
  "cargo": "Analista Judiciário",
  "nivel": "SUPERIOR",
  "modalidade": "A_E",
  "totalQuestoes": 0,
  "criadoEm": "2026-03-21T10:15:30Z"
}
```

## Respostas possíveis

### 201 — Created

Prova criada com sucesso.

### 400 — Bad Request

Falha de validação do corpo da requisição.

### 404 — Not Found

Instituição informada não existe no catálogo.

### 409 — Conflict

Já existe uma prova com o mesmo cabeçalho.

O serviço retorna `ProblemDetail` com:

- `title`: `Prova duplicada`
- `type`: `https://concurseiro.dev/errors/prova-duplicada`
- `detail`: `Já existe uma prova cadastrada com esse cabeçalho`

### 401 / 403

O `README.md` informa que endpoints autenticados só podem ser usados por usuários autenticados e ativos. O `openapi.json` também descreve 401 e 403 para este endpoint.

---

# GET /api/v1/provas/{id}

Busca uma prova por identificador.

## Descrição

Retorna um `ProvaResponse` com os dados da prova e a quantidade total de questões associadas.

## Path params

| Parâmetro | Tipo | Obrigatório | Descrição |
| --- | --- | --- | --- |
| id | number | sim | id da prova |

## Exemplo

```http
GET /api/v1/provas/1
```

## Resposta de sucesso

### 200 — OK

```json
{
  "id": 1,
  "banca": "CESPE",
  "instituicao": "PC-BA",
  "instituicaoId": 1,
  "ano": 2024,
  "cargo": "Analista Judiciário",
  "nivel": "SUPERIOR",
  "modalidade": "A_E",
  "totalQuestoes": 12,
  "criadoEm": "2026-03-21T10:15:30Z"
}
```

## Respostas possíveis

### 200 — OK

Prova encontrada.

### 404 — Not Found

Prova não encontrada.

### 401 / 403

O `openapi.json` lista essas respostas, mas o `README.md` descreve esse endpoint como público. Para documentação funcional, o comportamento esperado é leitura pública.

---

# GET /api/v1/provas

Lista provas cadastradas com paginação.

## Descrição

Retorna `Page<ProvaResponse>` ordenada por `criadoEm` decrescente.

## Query params

| Parâmetro | Tipo | Obrigatório | Padrão | Descrição |
| --- | --- | --- | --- | --- |
| page | number | não | 0 | número da página |
| size | number | não | 20 | quantidade por página |

## Exemplo

```http
GET /api/v1/provas?page=0&size=20
```

## Resposta de sucesso

### 200 — OK

```json
{
  "content": [
    {
      "id": 1,
      "banca": "CESPE",
      "instituicao": "PC-BA",
      "instituicaoId": 1,
      "ano": 2024,
      "cargo": "Analista Judiciário",
      "nivel": "SUPERIOR",
      "modalidade": "A_E",
      "totalQuestoes": 12,
      "criadoEm": "2026-03-21T10:15:30Z"
    }
  ],
  "page": {
    "size": 20,
    "number": 0,
    "totalElements": 1,
    "totalPages": 1
  }
}
```

## Respostas possíveis

### 200 — OK

Lista paginada retornada com sucesso.

### 400 — Bad Request

Parâmetros de paginação inválidos.

### 401 / 403

O `openapi.json` lista essas respostas, mas o `README.md` descreve a listagem de provas como pública.

---

# POST /api/v1/provas/{provaId}/questoes

Cadastra uma nova questão dentro de uma prova existente.

## Descrição

Recebe `ProvaQuestaoRequest`, valida o corpo com `@Valid`, valida o gabarito conforme a modalidade da prova e salva uma nova questão já associada à prova.

## Path params

| Parâmetro | Tipo | Obrigatório | Descrição |
| --- | --- | --- | --- |
| provaId | number | sim | id da prova |

## Request body real do código

```json
{
  "enunciado": "Texto introdutório da questão",
  "questao": "Quanto é 2 + 2?",
  "alternativas": "A) 1\nB) 2\nC) 3\nD) 4\nE) 5",
  "disciplinaId": 1,
  "assuntoId": 1,
  "subassunto": "Operações básicas",
  "gabarito": "D"
}
```

## Campos do request

| Campo | Tipo | Obrigatório | Regras |
| --- | --- | --- | --- |
| enunciado | string | sim | `@NotBlank`, máximo 10000 |
| questao | string | sim | `@NotBlank`, máximo 10000 |
| alternativas | string | não | máximo 10000 |
| disciplinaId | number | sim | `@NotNull` |
| assuntoId | number | sim | `@NotNull` |
| subassunto | string | não | sem validação declarada no DTO |
| gabarito | string | sim | `@NotBlank` |

## Resposta de sucesso

### 201 — Created

```json
{
  "idQuestion": "Q123",
  "enunciado": "Texto introdutório da questão",
  "questao": "Quanto é 2 + 2?",
  "alternativas": "A) 1\nB) 2\nC) 3\nD) 4\nE) 5",
  "disciplina": "Matemática",
  "disciplinaId": 1,
  "assunto": "Aritmética",
  "assuntoId": 1,
  "banca": "CESPE",
  "bancaId": 2,
  "instituicao": "PC-BA",
  "instituicaoId": 1,
  "ano": 2024,
  "cargo": "Analista Judiciário",
  "nivel": "SUPERIOR",
  "modalidade": "A_E",
  "gabarito": "D",
  "provaId": 1,
  "criadoEm": "2026-03-21T10:20:00Z"
}
```

## Respostas possíveis

### 201 — Created

Questão criada e vinculada à prova com sucesso.

### 400 — Bad Request

Falha de validação do corpo ou gabarito incompatível com a modalidade da prova.

### 404 — Not Found

Pode ocorrer quando algum recurso referenciado não existe:

- prova não encontrada
- disciplina não encontrada no catálogo
- assunto não encontrado no catálogo
- instituição da prova não encontrada no catálogo
- banca da prova não encontrada no catálogo

### 401 / 403

O `README.md` indica que endpoints autenticados exigem usuário ativo. O `openapi.json` também lista 401 e 403 para este endpoint.

---

# DTOs de resposta do módulo

## `ProvaResponse`

| Campo | Tipo |
| --- | --- |
| id | Long |
| banca | String |
| instituicao | String |
| instituicaoId | Long |
| ano | Integer |
| cargo | String |
| nivel | String |
| modalidade | String |
| totalQuestoes | Long |
| criadoEm | OffsetDateTime |

## `QuestaoResponse` retornado ao lançar questão

| Campo | Tipo |
| --- | --- |
| idQuestion | String |
| enunciado | String |
| questao | String |
| alternativas | String |
| disciplina | String |
| disciplinaId | Long |
| assunto | String |
| assuntoId | Long |
| banca | String |
| bancaId | Long |
| instituicao | String |
| instituicaoId | Long |
| ano | Integer |
| cargo | String |
| nivel | String |
| modalidade | String |
| gabarito | String |
| provaId | Long |
| criadoEm | OffsetDateTime |

---

# Segurança

## O que está claro no repositório

O `README.md` afirma que:

- endpoints de leitura podem ser públicos
- somente usuários **ATIVOS** podem usar endpoints autenticados
- usuários com role `VISITANTE` e `ADMIN` podem executar ações autenticadas comuns

## Aplicação prática para o módulo de provas

Documentação funcional mais segura:

- `GET /api/v1/provas` deve ser tratado como público
- `GET /api/v1/provas/{id}` deve ser tratado como público
- `POST /api/v1/provas` exige autenticação
- `POST /api/v1/provas/{provaId}/questoes` exige autenticação

Como o arquivo de configuração de segurança não foi usado como fonte primária nesta atualização, essa parte foi alinhada pelo `README.md` e pelo `openapi.json`.

---

# Divergências encontradas no repositório

Durante a análise, estas inconsistências apareceram e precisam ser consideradas para não documentar errado.

## 1. `docs/modelos/prova.md` está desatualizado

O documento do modelo ainda fala em campos como `nome`, `bancaId` e associação de questão por `idQuestion`, mas o código atual trabalha com:

- `banca` como texto
- `instituicaoId`
- `cargo`
- `nivel`
- `modalidade`
- criação de uma nova questão via `ProvaQuestaoRequest`

## 2. `openapi.json` tem schema inconsistente para `ProvaQuestaoRequest`

No código atual, `ProvaQuestaoRequest` possui:

- `enunciado`
- `questao`
- `alternativas`
- `disciplinaId`
- `assuntoId`
- `subassunto`
- `gabarito`

Mas o `openapi.json` ainda expõe propriedades antigas como `disciplina` e `assunto` e marca obrigatoriedades que não batem com o DTO Java.

Para este documento, o **código-fonte foi tratado como verdade principal**.

## 3. Resposta do controller não está envelopada em `success/data`

O estado anterior da documentação mostrava respostas assim:

```json
{
  "success": true,
  "data": { ... }
}
```

No controller atual, os métodos retornam diretamente `ProvaResponse`, `QuestaoResponse` ou `Page<ProvaResponse>`. Portanto, a documentação atualizada não usa esse envelope.

---

# Relação com outros documentos

Documentos relacionados no projeto:

```text
docs/autenticacao.md
docs/erros.md
docs/paginacao.md
docs/modelos/prova.md
docs/endpoints/questoes.md
```

Recomendação: atualizar também `docs/modelos/prova.md` e regenerar o `openapi.json` para eliminar as divergências apontadas acima.

---

# Resumo final

O módulo de provas hoje possui **4 operações**:

1. criar prova
2. buscar prova por id
3. listar provas paginadas
4. cadastrar uma nova questão dentro de uma prova

Os pontos mais importantes do estado atual são:

- prova tem unicidade por cabeçalho
- listagem ordena por `criadoEm desc`
- `totalQuestoes` é calculado dinamicamente
- o endpoint de lançamento cria uma questão nova, não reaproveita uma existente
- a documentação antiga e parte do `openapi.json` estão defasadas em relação ao código
