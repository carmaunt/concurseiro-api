# Auth API — Endpoints de Autenticação

Este documento descreve os endpoints responsáveis por **registro e autenticação de usuários** na Concurseiro API.

Esses endpoints permitem que clientes obtenham um **token JWT** que será utilizado para acessar rotas protegidas da aplicação.

---

# Base path

Todos os endpoints deste módulo utilizam o seguinte prefixo:

```text
/api/v1/auth
```

---

# POST /api/v1/auth/register

Cria um novo usuário na plataforma.

## Descrição

Esse endpoint permite registrar um novo usuário no sistema.

Após o cadastro, o usuário é criado com:

- status **PENDENTE**
- role **VISITANTE**

Usuários pendentes não podem realizar login imediatamente.

Um administrador deve aprovar o usuário para que ele se torne **ATIVO** e possa autenticar na API.

---

## Request

### Body

```json
{
  "nome": "Maria da Silva",
  "email": "maria@email.com",
  "senha": "123456"
}
```

---

## Campos

| Campo | Tipo   | Obrigatório | Descrição                  |
| ----- | ------ | ----------- | -------------------------- |
| nome  | string | sim         | nome completo do usuário   |
| email | string | sim         | email utilizado para login |
| senha | string | sim         | senha do usuário           |

Restrições aplicadas:
- `nome`: obrigatório, máximo de 160 caracteres
- `email`: obrigatório, formato de email, máximo de 200 caracteres
- `senha`: obrigatória, mínimo de 6 e máximo de 200 caracteres

---

## Status inicial do usuário

Usuários registrados por este endpoint são criados com:

status: **PENDENTE**  
role: **VISITANTE**

Esses valores são definidos automaticamente pelo sistema.

---

## Respostas

### 201 — Created

Usuário registrado com sucesso.

A implementação atual não retorna payload no corpo do endpoint; o padrão de resposta da API pode embrulhar o retorno conforme a infraestrutura de responses.

### 400 — Bad Request

Dados inválidos enviados na requisição.

Exemplos:
- erro de validação de campos
- email já cadastrado

A implementação atual usa `ResponseStatusException` com mensagem de domínio para email duplicado.

### 500 — Internal Server Error

Erro inesperado no servidor.

---

# POST /api/v1/auth/login

Realiza autenticação de um usuário existente.

---

## Descrição

O usuário envia email e senha.

Se as credenciais forem válidas, a API retorna um **token JWT**.

Esse token deverá ser utilizado nas requisições subsequentes.

O endpoint possui proteção contra abuso por limite de tentativas de login.

---

## Request

### Body

```json
{
  "email": "usuario@email.com",
  "senha": "123456"
}
```

---

## Campos

| Campo | Tipo   | Obrigatório | Descrição        |
| ----- | ------ | ----------- | ---------------- |
| email | string | sim         | email do usuário |
| senha | string | sim         | senha do usuário |

Restrições aplicadas:
- `email`: obrigatório, formato de email, máximo de 200 caracteres
- `senha`: obrigatória, mínimo de 6 e máximo de 200 caracteres

---

## Resposta de sucesso

### 200 — OK

```json
{
  "success": true,
  "data": {
    "token": "jwt_token",
    "email": "usuario@email.com",
    "role": "VISITANTE"
  }
}
```

---

## Respostas de erro

### 401 — Unauthorized

Ocorre quando as credenciais são inválidas.

Exemplo compatível com a estrutura atual de erro:

```json
{
  "type": "https://concurseiro.dev/errors/auth",
  "title": "Credenciais inválidas",
  "status": 401
}
```

### 403 — Forbidden

Ocorre quando o usuário existe, mas não está ativo.

Exemplo compatível com a estrutura atual de erro:

```json
{
  "type": "about:blank",
  "title": "Forbidden",
  "status": 403,
  "detail": "Usuário ainda não aprovado"
}
```

### 429 — Too Many Requests

Ocorre quando o limite de tentativas no endpoint de login é excedido.

Exemplo:

```json
{
  "type": "https://concurseiro.dev/errors/rate-limit",
  "title": "Muitas tentativas",
  "status": 429,
  "detail": "Limite de tentativas excedido. Tente novamente em breve."
}
```

### 500 — Internal Server Error

Erro inesperado no servidor.

---

## Campos da resposta

| Campo | Tipo   | Descrição                              |
| ----- | ------ | -------------------------------------- |
| token | string | token JWT utilizado para autenticação  |
| email | string | email do usuário autenticado           |
| role  | string | perfil do usuário (VISITANTE ou ADMIN) |

---

# Uso do token

Após o login, o token deve ser enviado em todas as requisições protegidas.

Header utilizado:

```http
Authorization: Bearer <token>
```

---

# Exemplo de requisição autenticada

```http
GET /api/v1/questoes
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

Se o token for válido, o acesso ao endpoint será permitido.

Caso contrário, a API retornará:

```text
401 Unauthorized
```

Em casos onde o usuário não possui permissão para acessar o recurso solicitado, a API poderá retornar:

```text
403 Forbidden
```

---

# Fluxo de autenticação

Fluxo completo do processo de login:

1. usuário realiza cadastro na API
2. o usuário é criado com status **PENDENTE**
3. um administrador aprova o usuário
4. o usuário realiza login com email e senha
5. a API valida as credenciais
6. um token JWT é gerado
7. o token é retornado ao cliente
8. o cliente envia o token nas requisições autenticadas
9. o Spring Security valida o token em cada requisição

---

# Rate limit no login

O endpoint `POST /api/v1/auth/login` possui limitação de tentativas para reduzir abuso e força bruta.

A limitação considera:
- IP do cliente
- email enviado no corpo da requisição

A implementação atual utiliza Redis para controle da janela de tentativas.

---

# Segurança

Boas práticas aplicadas no sistema:

* autenticação baseada em JWT
* endpoints protegidos pelo Spring Security
* validação de token em cada requisição
* controle de acesso por roles
* limitação de tentativas no endpoint de login

---

# Relação com outros documentos

Para entender completamente o funcionamento da autenticação consulte também:

```text
docs/autenticacao.md
```

Esse documento descreve em detalhes o funcionamento do sistema de autenticação da API.
