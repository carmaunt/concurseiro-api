# Provas API — Endpoints de Provas

Este documento descreve os endpoints do módulo de **provas** da Concurseiro API.

Esse módulo é responsável pelo cadastro de provas, consulta de provas existentes, listagem paginada e lançamento de questões dentro de uma prova.

---

# Base path

Todos os endpoints deste módulo utilizam o seguinte prefixo:

```http
/api/v1/provas
```

Isso está definido diretamente no `@RequestMapping` do `ProvaController`. ([raw.githubusercontent.com](https://raw.githubusercontent.com/carmaunt/concurseiro-api/main/src/main/java/br/com/concurseiro/api/prova/controller/ProvaController.java))

---

# Visão geral

As rotas do módulo de provas permitem:

* criar o cabeçalho de uma prova
* buscar uma prova por identificador
* listar provas cadastradas com paginação
* lançar uma questão dentro de uma prova

Essas rotas foram confirmadas diretamente no código do `ProvaController`. ([raw.githubusercontent.com](https://raw.githubusercontent.com/carmaunt/concurseiro-api/main/src/main/java/br/com/concurseiro/api/prova/controller/ProvaController.java))

---

# POST /api/v1/provas

Cria uma nova prova.

## Descrição

Esse endpoint recebe um `ProvaRequest`, valida o corpo da requisição com `@Valid` e retorna um `ProvaResponse`. O status retornado é **201 Created**. ([raw.githubusercontent.com](https://raw.githubusercontent.com/carmaunt/concurseiro-api/main/src/main/java/br/com/concurseiro/api/prova/controller/ProvaController.java))

## Request

```json
{
  "nome": "TRF 1 - Analista Judiciário",
  "ano": 2024,
  "bancaId": 1,
  "instituicaoId": 2
}
```

## Observações

* o payload exato depende da estrutura de `ProvaRequest`
* o controller valida o corpo com `@Valid`
* a resposta é serializada como `ProvaResponse` ([raw.githubusercontent.com](https://raw.githubusercontent.com/carmaunt/concurseiro-api/main/src/main/java/br/com/concurseiro/api/prova/controller/ProvaController.java))

## Respostas

### 201 — Created

Prova criada com sucesso. ([raw.githubusercontent.com](https://raw.githubusercontent.com/carmaunt/concurseiro-api/main/src/main/java/br/com/concurseiro/api/prova/controller/ProvaController.java))

### 400 — Bad Request

Dados inválidos enviados na requisição.

### 401 — Unauthorized

Requisição sem autenticação válida, caso a segurança da rota exija token.

### 500 — Internal Server Error

Erro inesperado no servidor.

---

# GET /api/v1/provas/{id}

Busca uma prova pelo identificador.

## Descrição

Retorna uma prova específica a partir do `id`. A resposta é serializada como `ProvaResponse`. ([raw.githubusercontent.com](https://raw.githubusercontent.com/carmaunt/concurseiro-api/main/src/main/java/br/com/concurseiro/api/prova/controller/ProvaController.java))

## Parâmetros de caminho

| Parâmetro | Tipo   | Descrição              |
| --------- | ------ | ---------------------- |
| id        | number | identificador da prova |

## Exemplo

```http
GET /api/v1/provas/1
```

## Resposta

```json
{
  "id": 1,
  "nome": "TRF 1 - Analista Judiciário",
  "ano": 2024
}
```

## Respostas

### 200 — OK

Prova encontrada. ([raw.githubusercontent.com](https://raw.githubusercontent.com/carmaunt/concurseiro-api/main/src/main/java/br/com/concurseiro/api/prova/controller/ProvaController.java))

### 404 — Not Found

Prova não encontrada.

---

# GET /api/v1/provas

Lista provas cadastradas com paginação.

## Descrição

Esse endpoint retorna uma `Page` do Spring Data. O controller aceita os parâmetros `page` e `size`, com valores padrão **0** e **20**, respectivamente. ([raw.githubusercontent.com](https://raw.githubusercontent.com/carmaunt/concurseiro-api/main/src/main/java/br/com/concurseiro/api/prova/controller/ProvaController.java))

## Query parameters suportados

| Parâmetro | Tipo   | Descrição                      |
| --------- | ------ | ------------------------------ |
| page      | number | número da página, começa em 0  |
| size      | number | quantidade de itens por página |

## Regras confirmadas no controller

| Regra       | Valor |
| ----------- | ----- |
| page padrão | 0     |
| size padrão | 20    |

O `ProvaController` não explicita validações adicionais para limites mínimos ou máximos desses parâmetros, apenas define os valores padrão. ([raw.githubusercontent.com](https://raw.githubusercontent.com/carmaunt/concurseiro-api/main/src/main/java/br/com/concurseiro/api/prova/controller/ProvaController.java))

## Exemplo

```http
GET /api/v1/provas?page=0&size=20
```

## Exemplo de resposta

```json
{
  "content": [
    {
      "id": 1,
      "nome": "TRF 1 - Analista Judiciário",
      "ano": 2024
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 30,
  "totalPages": 2
}
```

## Respostas

### 200 — OK

Lista paginada retornada com sucesso. ([raw.githubusercontent.com](https://raw.githubusercontent.com/carmaunt/concurseiro-api/main/src/main/java/br/com/concurseiro/api/prova/controller/ProvaController.java))

### 400 — Bad Request

Parâmetros de paginação inválidos, quando aplicável.

---

# POST /api/v1/provas/{provaId}/questoes

Lança uma questão em uma prova.

## Descrição

Esse endpoint recebe o identificador da prova no caminho e um `ProvaQuestaoRequest` no corpo da requisição. O corpo é validado com `@Valid`. A resposta retornada é um `QuestaoResponse` e o status é **201 Created**. ([raw.githubusercontent.com](https://raw.githubusercontent.com/carmaunt/concurseiro-api/main/src/main/java/br/com/concurseiro/api/prova/controller/ProvaController.java))

## Parâmetros de caminho

| Parâmetro | Tipo   | Descrição              |
| --------- | ------ | ---------------------- |
| provaId   | number | identificador da prova |

## Request

```json
{
  "idQuestion": "Q123"
}
```

## Observações

* o payload exato depende da estrutura de `ProvaQuestaoRequest`
* o método do service chamado é `cadastrarQuestao(provaId, request)`
* a resposta é convertida por `QuestaoResponse.fromEntity(...)` ([raw.githubusercontent.com](https://raw.githubusercontent.com/carmaunt/concurseiro-api/main/src/main/java/br/com/concurseiro/api/prova/controller/ProvaController.java))

## Exemplo

```http
POST /api/v1/provas/1/questoes
```

## Resposta

```json
{
  "idQuestion": "Q123",
  "texto": "Enunciado da questão",
  "ano": 2024
}
```

## Respostas

### 201 — Created

Questão lançada na prova com sucesso. ([raw.githubusercontent.com](https://raw.githubusercontent.com/carmaunt/concurseiro-api/main/src/main/java/br/com/concurseiro/api/prova/controller/ProvaController.java))

### 400 — Bad Request

Dados inválidos enviados na requisição.

### 404 — Not Found

Prova não encontrada ou questão não encontrada.

---

# Segurança e permissões

O `ProvaController` não explicita anotações de autorização por método. Portanto, qualquer regra sobre rotas públicas, autenticadas ou administrativas depende da configuração global do Spring Security e deve ser documentada em conjunto com `docs/autenticacao.md`. ([raw.githubusercontent.com](https://raw.githubusercontent.com/carmaunt/concurseiro-api/main/src/main/java/br/com/concurseiro/api/prova/controller/ProvaController.java))

---

# Relação com outros documentos

Para complementar este módulo, consulte também:

```text
docs/paginacao.md
docs/erros.md
docs/autenticacao.md
docs/endpoints/questoes.md
```

Também é recomendável criar um documento separado para o modelo de dados de prova:

```text
docs/modelos/prova.md
```

---

# Observação importante

Os nomes exatos dos campos de `ProvaRequest`, `ProvaResponse` e `ProvaQuestaoRequest` não aparecem detalhados no trecho do controller consultado. Por isso, os exemplos de payload acima servem como modelo editorial da documentação e devem ser refinados quando os DTOs forem documentados diretamente a partir do código.
