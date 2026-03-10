# Auth API — Endpoints de Autenticação

Este documento descreve os endpoints responsáveis por **registro e autenticação de usuários** na Concurseiro API.

Esses endpoints permitem que clientes obtenham um **token JWT** que será utilizado para acessar rotas protegidas da aplicação.

---

# Base path

Todos os endpoints deste módulo utilizam o seguinte prefixo:

```
/api/v1/auth
```

---

# POST /api/v1/auth/register

Cria um novo usuário na plataforma.

## Descrição

Esse endpoint permite registrar um novo usuário no sistema.

Após o cadastro, o usuário poderá realizar login para obter um token JWT.

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

---

## Respostas

### 201 — Created

Usuário registrado com sucesso.

### 400 — Bad Request

Dados inválidos enviados na requisição.

Exemplo:

```json
{
  "status": 400,
  "message": "Dados inválidos"
}
```

### 409 — Conflict

Usuário já existente com o mesmo email.

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

---

## Resposta de sucesso

### 200 — OK

```json
{
  "token": "jwt_token",
  "email": "usuario@email.com",
  "role": "USER"
}
```

---

## Campos da resposta

| Campo | Tipo   | Descrição                             |
| ----- | ------ | ------------------------------------- |
| token | string | token JWT utilizado para autenticação |
| email | string | email do usuário autenticado          |
| role  | string | perfil do usuário                     |

---

# Uso do token

Após o login, o token deve ser enviado em todas as requisições protegidas.

Header utilizado:

```
Authorization: Bearer <token>
```

---

# Exemplo de requisição autenticada

```
GET /api/v1/questoes
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

Se o token for válido, o acesso ao endpoint será permitido.

Caso contrário, a API retornará:

```
401 Unauthorized
```

---

# Fluxo de autenticação

Fluxo completo do processo de login:

1. Cliente envia email e senha
2. API valida as credenciais
3. Token JWT é gerado
4. Token é retornado ao cliente
5. Cliente envia o token nas próximas requisições
6. Spring Security valida o token

---

# Segurança

Boas práticas aplicadas no sistema:

* autenticação baseada em JWT
* endpoints protegidos pelo Spring Security
* validação de token em cada requisição

---

# Relação com outros documentos

Para entender completamente o funcionamento da autenticação consulte também:

```
docs/autenticacao.md
```

Esse documento descreve em detalhes o funcionamento do sistema de autenticação da API.
