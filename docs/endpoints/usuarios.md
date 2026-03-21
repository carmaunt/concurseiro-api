# Usuários API — Endpoints de Usuários

Este documento descreve os endpoints administrativos do módulo de **usuários** da Concurseiro API.

As rotas deste módulo permitem listar usuários cadastrados, ativar usuários pendentes e excluir usuários visitantes.

---

# Base path

Todos os endpoints documentados aqui utilizam o prefixo:

```http
/api/v1/admin/usuarios
```

Esse mapeamento está definido em `AdminUsuarioController`. fileciteturn38file0

---

# Visão geral

O módulo administrativo de usuários possui três operações:

* listar usuários de forma paginada
* ativar um usuário pendente
* excluir um usuário visitante

As respostas públicas do módulo usam o DTO `UsuarioPublicoResponse`, que contém os campos `id`, `nome`, `email`, `role` e `status`. fileciteturn38file0 fileciteturn40file0

O modelo de usuário possui os papéis `ADMIN` e `VISITANTE`, e os status `PENDENTE` e `ATIVO`. fileciteturn41file0

---

# Estrutura de resposta

## UsuarioPublicoResponse

Estrutura retornada pelos endpoints de listagem e ativação:

| Campo  | Tipo   | Descrição |
| ------ | ------ | --------- |
| id     | number | identificador do usuário |
| nome   | string | nome do usuário |
| email  | string | email do usuário |
| role   | string | papel do usuário: `ADMIN` ou `VISITANTE` |
| status | string | status do usuário: `PENDENTE` ou `ATIVO` |

O DTO é gerado por `UsuarioPublicoResponse.from(...)`. fileciteturn40file0

## Exemplo

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

# GET /api/v1/admin/usuarios

Lista os usuários cadastrados de forma paginada.

## Descrição

Retorna um `Page<UsuarioPublicoResponse>`. O controller recebe `page` e `size`, e delega a operação ao serviço `listarPaginado(page, size)`. fileciteturn38file0

No serviço, a paginação é montada com ordenação por `criadoEm` em ordem decrescente. O tamanho da página é limitado com `Math.min(size, 50)`. fileciteturn39file0

## Query parameters

| Parâmetro | Tipo   | Obrigatório | Padrão | Observação |
| --------- | ------ | ----------- | ------ | ---------- |
| page      | number | não         | 0      | número da página |
| size      | number | não         | 20     | quantidade por página, limitada a 50 no serviço |

## Exemplo

```http
GET /api/v1/admin/usuarios?page=0&size=20
```

## Exemplo de resposta

```json
{
  "content": [
    {
      "id": 7,
      "nome": "Maria Silva",
      "email": "maria@email.com",
      "role": "VISITANTE",
      "status": "PENDENTE"
    },
    {
      "id": 1,
      "nome": "Administrador",
      "email": "admin@email.com",
      "role": "ADMIN",
      "status": "ATIVO"
    }
  ],
  "page": {
    "size": 20,
    "number": 0,
    "totalElements": 2,
    "totalPages": 1
  }
}
```

## Respostas

### 200 — OK

Lista paginada retornada com sucesso. fileciteturn38file0

### 401 — Unauthorized

Requisição sem autenticação válida.

### 403 — Forbidden

Usuário autenticado sem permissão para acessar a área administrativa.

---

# PATCH /api/v1/admin/usuarios/{id}/ativar

Ativa um usuário pendente.

## Descrição

Busca o usuário pelo `id`, altera o status para `ATIVO` e retorna o usuário atualizado como `UsuarioPublicoResponse`. fileciteturn38file0 fileciteturn39file0

A operação usa `UsuarioService.ativarUsuario(...)`, que lança `404 Not Found` quando o usuário não é encontrado. fileciteturn39file0

## Path parameter

| Parâmetro | Tipo   | Descrição |
| --------- | ------ | --------- |
| id        | number | identificador do usuário |

## Exemplo

```http
PATCH /api/v1/admin/usuarios/5/ativar
```

## Exemplo de resposta

```json
{
  "id": 5,
  "nome": "Maria Silva",
  "email": "maria@email.com",
  "role": "VISITANTE",
  "status": "ATIVO"
}
```

## Respostas

### 200 — OK

Usuário ativado com sucesso. fileciteturn38file0

### 404 — Not Found

Usuário não encontrado. fileciteturn39file0

### 401 — Unauthorized

Requisição sem autenticação válida.

### 403 — Forbidden

Usuário autenticado sem permissão para executar a operação.

---

# DELETE /api/v1/admin/usuarios/{id}

Exclui um usuário visitante.

## Descrição

Remove o usuário identificado pelo `id` e retorna **204 No Content** quando a exclusão é concluída. fileciteturn38file0

No serviço, a exclusão possui duas regras:

1. se o usuário não existir, retorna `404 Not Found`
2. se o usuário for `ADMIN`, retorna `403 Forbidden` com a mensagem `Não é permitido excluir um administrador` fileciteturn39file0

## Path parameter

| Parâmetro | Tipo   | Descrição |
| --------- | ------ | --------- |
| id        | number | identificador do usuário |

## Exemplo

```http
DELETE /api/v1/admin/usuarios/5
```

## Respostas

### 204 — No Content

Usuário excluído com sucesso. fileciteturn38file0

### 404 — Not Found

Usuário não encontrado. fileciteturn39file0

### 403 — Forbidden

Tentativa de excluir um usuário administrador. fileciteturn39file0

### 401 — Unauthorized

Requisição sem autenticação válida.

---

# Regras do modelo de usuário relevantes para integração

A entidade `Usuario` possui os seguintes campos persistidos:

| Campo     | Tipo            | Observação |
| --------- | --------------- | ---------- |
| id        | Long            | chave primária |
| nome      | String          | obrigatório, até 160 caracteres |
| email     | String          | obrigatório, único, até 200 caracteres |
| senhaHash | String          | obrigatório |
| role      | enum            | `ADMIN` ou `VISITANTE` |
| status    | enum            | `PENDENTE` ou `ATIVO` |
| criadoEm  | OffsetDateTime  | preenchido automaticamente |

Esses dados estão definidos em `Usuario`. fileciteturn41file0

O repositório do módulo disponibiliza `findByEmail(...)` e `existsByEmail(...)`. fileciteturn42file0

---

# Fluxo de ativação e autenticação

O serviço de usuários implementa o seguinte fluxo:

1. no cadastro de visitante, o usuário é criado com role `VISITANTE` e status `PENDENTE`
2. um administrador pode ativar esse usuário pelo endpoint `PATCH /api/v1/admin/usuarios/{id}/ativar`
3. apenas usuários com status `ATIVO` conseguem autenticar
4. autenticação com email ou senha inválidos retorna `401 Unauthorized`
5. autenticação de usuário não aprovado retorna `403 Forbidden` com a mensagem `Usuário ainda não aprovado` fileciteturn39file0

---

# Segurança

A especificação OpenAPI do projeto publica o grupo `Admin - Usuarios` com os endpoints:

* `GET /api/v1/admin/usuarios`
* `PATCH /api/v1/admin/usuarios/{id}/ativar`
* `DELETE /api/v1/admin/usuarios/{id}`

Esses endpoints aparecem sob o esquema global de autenticação bearer token JWT definido na API. fileciteturn23file0

---

# Arquivos principais do módulo

```text
src/main/java/br/com/concurseiro/api/usuarios/controller/AdminUsuarioController.java
src/main/java/br/com/concurseiro/api/usuarios/service/UsuarioService.java
src/main/java/br/com/concurseiro/api/usuarios/dto/UsuarioPublicoResponse.java
src/main/java/br/com/concurseiro/api/usuarios/model/Usuario.java
src/main/java/br/com/concurseiro/api/usuarios/repository/UsuarioRepository.java
```
