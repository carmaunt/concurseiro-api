# Documentação Oficial — Concurseiro API

Este documento define a estrutura oficial de documentação da **Concurseiro API** e serve como base para os arquivos dentro da pasta `docs/`.

---

# Estrutura oficial de documentação

A documentação deve ficar organizada da seguinte forma no repositório:

```
/docs

/docs/arquitetura.md
/docs/autenticacao.md
/docs/erros.md
/docs/paginacao.md

/docs/endpoints
/docs/endpoints/auth.md
/docs/endpoints/usuarios.md
/docs/endpoints/questoes.md
/docs/endpoints/provas.md
/docs/endpoints/comentarios.md
/docs/endpoints/catalogo.md

/docs/modelos
/docs/modelos/usuario.md
/docs/modelos/questao.md
/docs/modelos/prova.md
/docs/modelos/comentario.md
```

O `README.md` permanece na raiz com visão geral do projeto.

---

# Documentação de autenticação

Arquivo:

```
docs/endpoints/auth.md
```

Conteúdo recomendado para esse arquivo:

---

# Auth API

Endpoints responsáveis por **registro e autenticação de usuários**.

Base path:

```
/api/v1/auth
```

A API utiliza **JWT (JSON Web Token)** para autenticação.

---

# POST /api/v1/auth/register

Cria um novo usuário.

## Request

```json
{
  "nome": "Maria da Silva",
  "email": "maria@email.com",
  "senha": "123456"
}
```

## Campos

| Campo | Tipo | Obrigatório | Descrição |
|-----|-----|-----|-----|
| nome | string | sim | nome do usuário |
| email | string | sim | email do usuário |
| senha | string | sim | senha do usuário |

## Response

### 201 Created

Usuário criado com sucesso.

### Possíveis erros

| Código | Significado |
|------|------|
| 400 | dados inválidos |
| 401 | não autorizado |
| 500 | erro interno |

---

# POST /api/v1/auth/login

Realiza autenticação do usuário.

## Request

```json
{
  "email": "usuario@email.com",
  "senha": "123456"
}
```

## Response

```json
{
  "token": "jwt_token",
  "email": "usuario@email.com",
  "role": "USER"
}
```

## Campos

| Campo | Tipo | Descrição |
|-----|-----|-----|
| token | string | token JWT |
| email | string | email do usuário |
| role | string | papel do usuário |

---

# Utilização do token

Após autenticar, o cliente deve enviar o token no header:

```
Authorization: Bearer <token>
```

Exemplo de requisição autenticada:

```
GET /api/v1/questoes
Authorization: Bearer eyJhbGci...
```

---

# Fluxo de autenticação

1. Usuário envia credenciais
2. API valida email e senha
3. API gera token JWT
4. Cliente usa token nas requisições seguintes

---

# Próximos arquivos de documentação

Criar os seguintes arquivos:

```
docs/endpoints/questoes.md
docs/endpoints/provas.md
docs/endpoints/usuarios.md
docs/endpoints/catalogo.md
docs/endpoints/comentarios.md
```

O próximo módulo a documentar é **questões**, que é o núcleo funcional da API.

