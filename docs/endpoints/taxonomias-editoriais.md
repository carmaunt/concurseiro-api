# Taxonomias editoriais

Categorias e tags são gerenciadas exclusivamente por administradores. Todos os endpoints abaixo exigem `ROLE_ADMIN`.

## Categorias

- `GET /api/v1/admin/categorias-editoriais`: lista paginada; aceita `q`, `status`, `page` e `size`.
- `GET /api/v1/admin/categorias-editoriais/{id}`: detalhe.
- `GET /api/v1/admin/categorias-editoriais/ativas`: opções mínimas para formulários.
- `POST /api/v1/admin/categorias-editoriais`: cria.
- `PUT /api/v1/admin/categorias-editoriais/{id}`: atualiza.
- `PATCH /api/v1/admin/categorias-editoriais/{id}/status?status=ATIVA|ARQUIVADA`: arquiva ou reativa.

Payload:

```json
{
  "nome": "Dicas de estudo",
  "slug": "dicas-de-estudo",
  "descricao": "Conteúdos sobre organização e métodos de estudo"
}
```

## Tags

- `GET /api/v1/admin/tags-editoriais`: lista paginada; aceita `q`, `status`, `page` e `size`.
- `GET /api/v1/admin/tags-editoriais/{id}`: detalhe.
- `GET /api/v1/admin/tags-editoriais/ativas`: opções mínimas; aceita busca `q`.
- `POST /api/v1/admin/tags-editoriais`: cria.
- `PUT /api/v1/admin/tags-editoriais/{id}`: atualiza.
- `PATCH /api/v1/admin/tags-editoriais/{id}/status?status=ATIVA|ARQUIVADA`: arquiva ou reativa.

Payload:

```json
{
  "nome": "Revisão",
  "slug": "revisao"
}
```

## Regras

- Nome e slug são únicos sem diferenciar maiúsculas e minúsculas.
- O backend gera e normaliza o slug quando necessário.
- Registros arquivados não aparecem nos endpoints de opções ativas.
- Associações antigas são preservadas após arquivamento.
- A migration `024-normalize-conteudo-taxonomias` mantém os campos textuais legados e migra seus valores para as novas relações.
