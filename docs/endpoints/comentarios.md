# Comentários API — Endpoints de Comentários

Este documento descreve os endpoints responsáveis pelo gerenciamento de **comentários** na Concurseiro API.

O módulo de comentários permite que usuários registrem observações relacionadas a questões e interajam com comentários por meio de curtidas e descurtidas.

---

# Base paths

Os endpoints deste módulo estão distribuídos em dois prefixos:

```text
/api/v1/questoes/{questaoId}/comentarios
/api/v1/comentarios/{id}
```

---

# Visão geral

Na versão atual do projeto, o módulo de comentários permite:

* criar comentários vinculados a uma questão
* listar comentários de uma questão
* curtir comentários
* descurtir comentários

Não há, no estado atual do projeto, endpoint público documentado para:
* listar todos os comentários globalmente
* remover comentários
* editar comentários

---

# Estrutura do comentário

A resposta de comentário atualmente inclui os seguintes campos:

| Campo        | Tipo      | Descrição                                 |
| ------------ | --------- | ----------------------------------------- |
| id           | number    | identificador do comentário               |
| questaoId    | string    | identificador da questão associada        |
| autor        | string    | nome informado no momento da criação      |
| texto        | string    | conteúdo do comentário                    |
| curtidas     | number    | total de curtidas                         |
| descurtidas  | number    | total de descurtidas                      |
| criadoEm     | datetime  | data e hora de criação                    |

---

# POST /api/v1/questoes/{questaoId}/comentarios

Cria um novo comentário associado a uma questão.

## Descrição

Esse endpoint cria um comentário vinculado à questão informada no path.

A implementação atual grava:
- `questaoId`
- `autor`
- `texto`

Curtidas e descurtidas são mantidas no próprio comentário.

---

## Parâmetros de caminho

| Parâmetro | Tipo   | Descrição                |
| --------- | ------ | ------------------------ |
| questaoId | string | identificador da questão |

---

## Request

### Body

```json
{
  "autor": "Mauricio",
  "texto": "Essa questão costuma aparecer em provas da banca."
}
```

---

## Campos

| Campo | Tipo   | Obrigatório | Descrição                      |
| ----- | ------ | ----------- | ------------------------------ |
| autor | string | sim         | nome do autor do comentário    |
| texto | string | sim         | conteúdo do comentário         |

Restrições aplicadas:
- `autor`: obrigatório, máximo de 100 caracteres
- `texto`: obrigatório, máximo de 5000 caracteres

---

## Resposta

### 201 — Created

Exemplo de resposta compatível com a implementação atual:

```json
{
  "success": true,
  "data": {
    "id": 1,
    "questaoId": "Q123",
    "autor": "Mauricio",
    "texto": "Essa questão costuma aparecer em provas da banca.",
    "curtidas": 0,
    "descurtidas": 0,
    "criadoEm": "2026-03-03T21:30:00Z"
  }
}
```

Observação:
- a API versionada utiliza padronização de resposta, então o payload pode vir encapsulado em `success` e `data`

### 400 — Bad Request

Ocorre quando o corpo da requisição é inválido.

Exemplos:
- `autor` em branco
- `texto` em branco
- tamanho máximo excedido

### 401 — Unauthorized

Ocorre quando o usuário não está autenticado.

### 403 — Forbidden

Ocorre quando o usuário autenticado não possui acesso ao recurso.

---

# GET /api/v1/questoes/{questaoId}/comentarios

Lista os comentários associados a uma questão.

## Descrição

Retorna uma página de comentários da questão informada.

A ordenação atual aceita:
- `recentes` para ordenar por `criadoEm` desc
- `curtidas` para ordenar por `curtidas` desc

Qualquer outro valor atualmente cai no comportamento padrão de comentários mais recentes.

---

## Parâmetros de caminho

| Parâmetro | Tipo   | Descrição                |
| --------- | ------ | ------------------------ |
| questaoId | string | identificador da questão |

---

## Query parameters

| Parâmetro | Tipo   | Obrigatório | Descrição                                                |
| --------- | ------ | ----------- | -------------------------------------------------------- |
| page      | number | não         | número da página, padrão `0`                            |
| size      | number | não         | quantidade por página, padrão `20`, máximo efetivo `50` |
| ordenar   | string | não         | `recentes` ou `curtidas`, padrão `recentes`             |

---

## Exemplo

```http
GET /api/v1/questoes/Q123/comentarios?page=0&size=10&ordenar=curtidas
```

---

## Resposta

Exemplo compatível com a implementação atual:

```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "questaoId": "Q123",
        "autor": "Mauricio",
        "texto": "Essa questão costuma aparecer em provas da banca.",
        "curtidas": 4,
        "descurtidas": 0,
        "criadoEm": "2026-03-03T21:30:00Z"
      }
    ],
    "pageable": {},
    "last": true,
    "totalPages": 1,
    "totalElements": 1,
    "size": 10,
    "number": 0,
    "sort": {},
    "first": true,
    "numberOfElements": 1,
    "empty": false
  }
}
```

Observação:
- o tipo de retorno é `Page<ComentarioResponse>`
- a serialização exata da paginação depende da forma como o Spring serializa `Page` no ambiente configurado

---

# POST /api/v1/comentarios/{id}/curtir

Adiciona uma curtida ao comentário informado.

## Parâmetros de caminho

| Parâmetro | Tipo   | Descrição                   |
| --------- | ------ | --------------------------- |
| id        | number | identificador do comentário |

---

## Exemplo

```http
POST /api/v1/comentarios/1/curtir
```

---

## Respostas

### 200 — OK

Exemplo compatível com a implementação atual:

```json
{
  "success": true,
  "data": {
    "id": 1,
    "questaoId": "Q123",
    "autor": "Mauricio",
    "texto": "Essa questão costuma aparecer em provas da banca.",
    "curtidas": 5,
    "descurtidas": 0,
    "criadoEm": "2026-03-03T21:30:00Z"
  }
}
```

### 404 — Not Found

Comentário não encontrado.

---

# POST /api/v1/comentarios/{id}/descurtir

Adiciona uma descurtida ao comentário informado.

## Parâmetros de caminho

| Parâmetro | Tipo   | Descrição                   |
| --------- | ------ | --------------------------- |
| id        | number | identificador do comentário |

---

## Exemplo

```http
POST /api/v1/comentarios/1/descurtir
```

---

## Respostas

### 200 — OK

Exemplo compatível com a implementação atual:

```json
{
  "success": true,
  "data": {
    "id": 1,
    "questaoId": "Q123",
    "autor": "Mauricio",
    "texto": "Essa questão costuma aparecer em provas da banca.",
    "curtidas": 5,
    "descurtidas": 1,
    "criadoEm": "2026-03-03T21:30:00Z"
  }
}
```

### 404 — Not Found

Comentário não encontrado.

---

# Segurança

Na configuração atual de segurança:

* `GET /api/v1/questoes/{questaoId}/comentarios` é público
* `POST /api/v1/questoes/{questaoId}/comentarios` exige usuário autenticado com role `VISITANTE` ou `ADMIN`
* `POST /api/v1/comentarios/{id}/curtir` exige usuário autenticado com role `VISITANTE` ou `ADMIN`
* `POST /api/v1/comentarios/{id}/descurtir` exige usuário autenticado com role `VISITANTE` ou `ADMIN`

As regras são controladas pelo Spring Security.

---

# Relação com outros documentos

Para entender autenticação consulte:

```text
docs/autenticacao.md
```

Para entender paginação consulte:

```text
docs/paginacao.md
```

Para entender questões consulte:

```text
docs/endpoints/questoes.md
```

---

# Observação

Este documento foi ajustado para refletir apenas o que está implementado no projeto hoje.

Não foram incluídos endpoints de listagem global, edição ou remoção de comentários porque eles não aparecem na implementação atual do controller de comentários.
