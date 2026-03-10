# Usuários API — Endpoints de Usuários

Este documento descreve os endpoints relacionados ao gerenciamento de usuários na **Concurseiro API**.

Esses endpoints permitem consultar informações de usuários, além de realizar operações administrativas quando o usuário possui as permissões necessárias.

---

# Base path

Todos os endpoints deste módulo utilizam o seguinte prefixo:

```
/api/v1/usuarios
```

---

# Visão geral

O módulo de usuários é responsável por:

* consultar dados de usuários
* gerenciamento administrativo de contas
* controle de permissões

Algumas rotas são acessíveis apenas por **administradores**.

---

# GET /api/v1/usuarios

Lista usuários do sistema.

## Descrição

Retorna uma lista paginada de usuários cadastrados na plataforma.

Este endpoint normalmente é restrito a **usuários administradores**.

---

## Parâmetros de paginação

| Parâmetro | Tipo   | Descrição                          |
| --------- | ------ | ---------------------------------- |
| page      | number | número da página                   |
| size      | number | quantidade de registros por página |
| sort      | string | campo utilizado para ordenação     |

---

## Exemplo de requisição

```
GET /api/v1/usuarios?page=0&size=10
```

---

## Resposta

```json
{
  "content": [
    {
      "id": 1,
      "nome": "Maria Silva",
      "email": "maria@email.com",
      "role": "USER"
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 50,
  "totalPages": 5
}
```

---

# GET /api/v1/usuarios/{id}

Retorna os dados de um usuário específico.

---

## Parâmetros de caminho

| Parâmetro | Tipo   | Descrição                |
| --------- | ------ | ------------------------ |
| id        | number | identificador do usuário |

---

## Exemplo

```
GET /api/v1/usuarios/1
```

---

## Resposta

```json
{
  "id": 1,
  "nome": "Maria Silva",
  "email": "maria@email.com",
  "role": "USER"
}
```

---

# PUT /api/v1/usuarios/{id}

Atualiza informações de um usuário.

---

## Request

```json
{
  "nome": "Maria da Silva",
  "email": "maria@email.com"
}
```

---

## Campos

| Campo | Tipo   | Descrição        |
| ----- | ------ | ---------------- |
| nome  | string | nome do usuário  |
| email | string | email do usuário |

---

## Respostas

### 200 — OK

Usuário atualizado com sucesso.

### 404 — Not Found

Usuário não encontrado.

### 400 — Bad Request

Dados inválidos.

---

# DELETE /api/v1/usuarios/{id}

Remove um usuário do sistema.

Este endpoint normalmente é restrito a administradores.

---

## Exemplo

```
DELETE /api/v1/usuarios/1
```

---

## Respostas

### 204 — No Content

Usuário removido com sucesso.

### 404 — Not Found

Usuário não encontrado.

---

# Permissões

Dependendo da configuração de segurança da API, algumas operações podem exigir permissões administrativas.

Exemplos de restrições:

* listagem completa de usuários
* remoção de contas
* alteração de papéis de usuário

Essas regras são controladas pelo **Spring Security**.

---

# Relação com outros documentos

Para entender autenticação e permissões consulte:

```
docs/autenticacao.md
```

Para entender respostas paginadas consulte:

```
docs/paginacao.md
```
