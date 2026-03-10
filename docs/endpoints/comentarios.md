# Comentários API — Endpoints de Comentários

Este documento descreve os endpoints responsáveis pelo gerenciamento de **comentários** na Concurseiro API.

O módulo de comentários permite que usuários registrem observações e discussões relacionadas a entidades do sistema, como questões.

---

# Base path

Todos os endpoints deste módulo utilizam o seguinte prefixo:

```
/api/v1/comentarios
```

---

# Visão geral

O módulo de comentários permite:

* criar comentários
* listar comentários
* consultar comentários vinculados a uma entidade

Esses recursos permitem adicionar contexto colaborativo às questões cadastradas na plataforma.

---

# POST /api/v1/comentarios

Cria um novo comentário.

## Descrição

Esse endpoint recebe um objeto `ComentarioRequest` contendo o texto do comentário e a referência da entidade relacionada.

---

## Request

```json
{
  "texto": "Essa questão costuma aparecer em provas da FGV",
  "questaoId": "Q123"
}
```

## Campos

| Campo     | Tipo   | Obrigatório | Descrição                            |
| --------- | ------ | ----------- | ------------------------------------ |
| texto     | string | sim         | conteúdo do comentário               |
| questaoId | string | sim         | identificador da questão relacionada |

---

## Resposta

### 201 — Created

Comentário criado com sucesso.

```json
{
  "id": 1,
  "texto": "Essa questão costuma aparecer em provas da FGV",
  "questaoId": "Q123"
}
```

---

# GET /api/v1/comentarios

Lista comentários cadastrados.

## Descrição

Retorna uma lista paginada de comentários.

---

## Query parameters

| Parâmetro | Tipo   | Descrição               |
| --------- | ------ | ----------------------- |
| page      | number | número da página        |
| size      | number | quantidade de registros |

---

## Exemplo

```
GET /api/v1/comentarios?page=0&size=10
```

---

## Resposta

```json
{
  "content": [
    {
      "id": 1,
      "texto": "Comentário exemplo",
      "questaoId": "Q123"
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 40,
  "totalPages": 4
}
```

---

# GET /api/v1/comentarios/questao/{questaoId}

Lista comentários associados a uma questão.

## Parâmetros de caminho

| Parâmetro | Tipo   | Descrição                |
| --------- | ------ | ------------------------ |
| questaoId | string | identificador da questão |

---

## Exemplo

```
GET /api/v1/comentarios/questao/Q123
```

---

## Resposta

```json
[
  {
    "id": 1,
    "texto": "Comentário exemplo",
    "questaoId": "Q123"
  }
]
```

---

# DELETE /api/v1/comentarios/{id}

Remove um comentário.

---

## Parâmetros de caminho

| Parâmetro | Tipo   | Descrição                   |
| --------- | ------ | --------------------------- |
| id        | number | identificador do comentário |

---

## Exemplo

```
DELETE /api/v1/comentarios/1
```

---

## Respostas

### 204 — No Content

Comentário removido com sucesso.

### 404 — Not Found

Comentário não encontrado.

---

# Segurança

Dependendo da configuração de segurança da aplicação, a criação ou remoção de comentários pode exigir autenticação do usuário.

Essas regras são controladas pelo **Spring Security**.

---

# Relação com outros documentos

Para entender autenticação consulte:

```
docs/autenticacao.md
```

Para entender paginação consulte:

```
docs/paginacao.md
```

Para entender questões consulte:

```
docs/endpoints/questoes.md
```

---

# Observação

Os campos exatos de `ComentarioRequest` e `ComentarioResponse` devem ser confirmados diretamente nos DTOs do módulo de comentários quando o modelo de dados for documentado em `docs/modelos/comentario.md`.
