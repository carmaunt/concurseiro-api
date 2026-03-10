# Usuários API — Endpoints de Usuários

Este documento descreve os endpoints relacionados ao gerenciamento de usuários na **Concurseiro API**.

Esses endpoints permitem consultar informações de usuários, além de realizar operações administrativas quando o usuário possui as permissões necessárias.

---

# Base path

Todos os endpoints deste módulo utilizam o seguinte prefixo:

```
/api/v1/admin/usuarios
```

---

# Visão geral

O módulo de usuários possui endpoints administrativos utilizados para gerenciar contas do sistema.

Esses endpoints são acessíveis apenas por usuários com role **ADMIN**.

Principais operações administrativas:

* listar usuários
* aprovar usuários visitantes
* excluir usuários visitantes

---

# GET /api/v1/admin/usuarios

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
GET /api/v1/admin/usuarios?page=0&size=10
```

---

## Resposta

```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "nome": "Maria Silva",
        "email": "maria@email.com",
        "role": "VISITANTE",
        "status": "PENDENTE"
      }
    ]
  }
}
```

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

# PATCH /api/v1/admin/usuarios/{id}/ativar

Aprova um usuário visitante.

Esse endpoint altera o status do usuário de **PENDENTE** para **ATIVO**.

Apenas administradores podem executar esta operação.

## Exemplo

PATCH /api/v1/admin/usuarios/5/ativar

## Resposta

```json
{
  "success": true,
  "data": {
    "id": 5,
    "nome": "Maria Silva",
    "email": "maria@email.com",
    "role": "VISITANTE",
    "status": "ATIVO"
  }
}
```
---

# DELETE /api/v1/admin/usuarios/{id}

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

Todos os endpoints deste documento são restritos a usuários com role **ADMIN**.

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
