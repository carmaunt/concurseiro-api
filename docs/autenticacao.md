# Autenticação — Concurseiro API

Este documento descreve como funciona o sistema de autenticação da **Concurseiro API**, incluindo o fluxo de login, geração de tokens e utilização de autenticação baseada em **JWT (JSON Web Token)**.

---

# Visão geral

A Concurseiro API utiliza **JWT** para autenticar requisições.

Isso significa que, após realizar login, o cliente recebe um **token de acesso** que deve ser enviado em todas as requisições protegidas.

Esse modelo é **stateless**, ou seja, o servidor não precisa manter sessão do usuário.

---

# Fluxo de autenticação

Fluxo completo:

1. Cliente envia credenciais (email e senha)
2. A API valida o usuário
3. A API gera um token JWT
4. O token é retornado ao cliente
5. O cliente envia o token em requisições futuras
6. O Spring Security valida o token

---

# Endpoint de login

Endpoint responsável por autenticar usuários.

```
POST /api/v1/auth/login
```

## Request

```json
{
  "email": "usuario@email.com",
  "senha": "123456"
}
```

## Campos

| Campo | Tipo   | Obrigatório | Descrição        |
| ----- | ------ | ----------- | ---------------- |
| email | string | sim         | email do usuário |
| senha | string | sim         | senha do usuário |

---

# Resposta de autenticação

Quando as credenciais são válidas, a API retorna um token JWT.

```json
{
  "token": "jwt_token",
  "email": "usuario@email.com",
  "role": "USER"
}
```

## Campos da resposta

| Campo | Tipo   | Descrição         |
| ----- | ------ | ----------------- |
| token | string | token JWT         |
| email | string | email autenticado |
| role  | string | papel do usuário  |

---

# Uso do token

O token deve ser enviado no header **Authorization**.

```
Authorization: Bearer <token>
```

Exemplo:

```
GET /api/v1/questoes
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

Se o token for válido, o usuário poderá acessar o endpoint.

---

# Validação do token

A validação do token é realizada pelo **Spring Security**.

Durante cada requisição:

1. O header Authorization é interceptado
2. O token JWT é extraído
3. A assinatura do token é validada
4. O usuário é carregado no contexto de segurança

Se o token for inválido ou expirado, a requisição é rejeitada.

---

# Expiração do token

Tokens possuem tempo de expiração configurado na aplicação.

Após expirar:

* o cliente deve realizar login novamente
* um novo token será emitido

---

# Rotas públicas

Alguns endpoints não exigem autenticação.

Exemplo:

```
POST /api/v1/auth/login
POST /api/v1/auth/register
```

Essas rotas são utilizadas para acesso inicial ao sistema.

---

# Rotas protegidas

A maioria da API exige autenticação.

Exemplo:

```
GET /api/v1/questoes
POST /api/v1/comentarios
GET /api/v1/provas
```

Se o token não for enviado ou for inválido, a API retorna:

```
401 Unauthorized
```

---

# Segurança

Boas práticas implementadas na API:

* autenticação baseada em token
* validação pelo Spring Security
* endpoints protegidos por autenticação
* uso de HTTPS recomendado em produção

---

# Resumo

O sistema de autenticação da Concurseiro API utiliza JWT para permitir acesso seguro aos endpoints da aplicação, garantindo que apenas usuários autenticados possam acessar recursos protegidos.
