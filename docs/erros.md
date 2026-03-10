# Tratamento de Erros — Concurseiro API

Este documento descreve como a **Concurseiro API** lida com erros, quais códigos HTTP podem ser retornados e como interpretar as respostas de erro da aplicação.

---

# Visão geral

A API utiliza **códigos de status HTTP padrão** para indicar o resultado de uma requisição.

Quando ocorre um erro, a resposta normalmente inclui:

* código HTTP
* mensagem de erro
* detalhes adicionais quando disponíveis

Isso permite que aplicações cliente tratem falhas de forma consistente.

---

# Estrutura padrão de erro

Em geral, respostas de erro seguem uma estrutura semelhante a:

```json
{
  "timestamp": "2026-03-09T10:15:30Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Dados inválidos",
  "path": "/api/v1/questoes"
}
```

## Campos

| Campo     | Descrição                     |
| --------- | ----------------------------- |
| timestamp | momento em que o erro ocorreu |
| status    | código HTTP da resposta       |
| error     | descrição padrão do status    |
| message   | mensagem detalhada do erro    |
| path      | endpoint que gerou o erro     |

---

# Códigos HTTP utilizados

## 200 — OK

A requisição foi processada com sucesso.

Exemplo:

* consulta de questões
* autenticação válida

---

## 201 — Created

Um novo recurso foi criado com sucesso.

Exemplo:

* criação de usuário
* criação de questão
* criação de comentário

---

## 400 — Bad Request

A requisição enviada pelo cliente possui dados inválidos.

Causas comuns:

* campos obrigatórios ausentes
* formato inválido
* validação de dados falhou

Exemplo de resposta:

```json
{
  "status": 400,
  "message": "Campo email é obrigatório"
}
```

---

## 401 — Unauthorized

O cliente não está autenticado ou o token JWT é inválido.

Situações comuns:

* token ausente
* token expirado
* token inválido

---

## 403 — Forbidden

O usuário está autenticado, mas não possui permissão para acessar o recurso.

Exemplo:

* tentativa de acessar endpoint administrativo sem permissão.

---

## 404 — Not Found

O recurso solicitado não foi encontrado.

Exemplo:

* questão inexistente
* usuário inexistente

---

## 409 — Conflict

Ocorre quando existe conflito de dados.

Exemplo:

* tentativa de criar usuário com email já existente.

---

## 500 — Internal Server Error

Erro interno inesperado da aplicação.

Esse erro indica falha no servidor e deve ser investigado pelos desenvolvedores.

---

# Erros de validação

Quando validações de dados falham, a API pode retornar mensagens indicando quais campos possuem problemas.

Exemplo:

```json
{
  "status": 400,
  "message": "Erro de validação",
  "errors": [
    "email deve ser válido",
    "senha deve possuir pelo menos 6 caracteres"
  ]
}
```

---

# Tratamento no cliente

Aplicações cliente devem tratar erros utilizando o código HTTP retornado.

Exemplo de fluxo comum:

* **401** → redirecionar usuário para login
* **403** → mostrar mensagem de acesso negado
* **404** → mostrar recurso não encontrado
* **500** → exibir erro genérico

---

# Boas práticas

Para integração correta com a API:

* sempre verificar o código HTTP
* tratar erros de autenticação
* validar dados antes de enviar requisições
* registrar logs de erro no cliente

---

# Resumo

A Concurseiro API utiliza códigos HTTP padrão e respostas estruturadas para garantir que erros possam ser interpretados facilmente por aplicações cliente e ferramentas de integração.
