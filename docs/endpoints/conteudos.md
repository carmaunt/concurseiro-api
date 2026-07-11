# Conteúdos API — Portal Público

Endpoints para alimentar o portal público do O Concurseiro com dados gerenciados pelo painel admin.

## Tipos

- `NOTICIA`
- `BLOG`
- `CONCURSO_ABERTO`
- `EDITAL_PREVISTO`

## Status

- `RASCUNHO`
- `PUBLICADO`

Conteúdos em `RASCUNHO` não aparecem nos endpoints públicos.

## Endpoints públicos

### GET `/api/v1/conteudos`

Lista conteúdos publicados.

Query params:

- `tipo`: opcional.
- `q`: busca por título, resumo ou conteúdo.
- `search`: alias público de `q`; útil para URLs da web.
- `category`: slug público da categoria.
- `tag`: slug público da tag.
- `categoria`: filtro legado por nome, mantido para compatibilidade.
- `page`: padrão `0`.
- `size`: padrão `10`, máximo `50`.
- `limit`: alias de `size`.

Regras públicas:

- retorna somente `status=PUBLICADO`;
- exige `publicadoEm` preenchido;
- exige `publicadoEm <= agora`;
- não retorna rascunhos nem publicações agendadas para o futuro.
- combina busca, categoria, tag e paginação no banco.
- categorias ou tags arquivadas não são aceitas como filtros públicos.

Exemplo:

```text
GET /api/v1/conteudos?tipo=NOTICIA&search=concurso&category=seguranca-publica&tag=policia-federal&page=0&limit=9
```

### GET `/api/v1/categorias/publicas?tipo=NOTICIA`

Retorna `id`, `nome` e `slug` somente das categorias ativas que possuem ao menos um conteúdo publicado e vigente do tipo solicitado.

### GET `/api/v1/tags/publicas?tipo=NOTICIA`

Retorna `id`, `nome` e `slug` somente das tags ativas que possuem ao menos um conteúdo publicado e vigente do tipo solicitado.

### GET `/api/v1/conteudos/destaques`

Lista conteúdos publicados marcados como destaque.

Query params:

- `page`: padrão `0`.
- `size`: padrão `5`, máximo `50`.

Aplica as mesmas regras públicas de publicação.

### GET `/api/v1/conteudos/{tipo}/{slug}`

Busca um conteúdo publicado pelo tipo e slug.

Aplica as mesmas regras públicas de publicação.

## Endpoints administrativos

Todos os endpoints administrativos usam o prefixo:

```text
/api/v1/admin/conteudos
```

Exigem permissão de admin via configuração global de segurança.

### GET `/api/v1/admin/conteudos`

Lista todos os conteúdos, incluindo rascunhos.

Query params:

- `tipo`: opcional.
- `status`: opcional.
- `q`: busca por título ou resumo.
- `page`: padrão `0`.
- `size`: padrão `10`, máximo `50`.

### GET `/api/v1/admin/conteudos/{id}`

Busca um conteúdo por ID.

### POST `/api/v1/admin/conteudos`

Cria conteúdo.

Campos principais:

- `titulo`
- `slug`
- `resumo`
- `conteudo`
- `imagemCapa`
- `categoriaId`: ID opcional de uma categoria editorial.
- `tagIds`: lista de IDs de tags editoriais.
- `status`
- `tipo`
- `destaque`
- `seoTitulo`
- `seoDescricao`

### PUT `/api/v1/admin/conteudos/{id}`

Atualiza conteúdo.

### PATCH `/api/v1/admin/conteudos/{id}/status?status=PUBLICADO`

Publica ou despublica conteúdo.

### DELETE `/api/v1/admin/conteudos/{id}`

Exclui conteúdo.

## Regras

- O slug é normalizado pelo backend.
- O slug deve ser único dentro do mesmo tipo.
- Ao publicar, `publicadoEm` é preenchido automaticamente quando ainda não existir.
- Ao voltar para rascunho, `publicadoEm` é limpo.
- A web pública deve consumir somente endpoints públicos.
- Categorias e tags arquivadas permanecem em conteúdos antigos, mas não podem ser associadas a novos conteúdos.

## Contrato de taxonomias

As respostas mantêm `categoria` como texto de compatibilidade e também retornam a estrutura normalizada:

```json
{
  "categoria": "Dicas de estudo",
  "category": { "id": 1, "nome": "Dicas de estudo", "slug": "dicas-de-estudo" },
  "tags": [
    { "id": 2, "nome": "Revisão", "slug": "revisao" }
  ]
}
```

O campo `tags`, anteriormente textual, agora é uma lista estruturada. Os clientes web e admin foram atualizados junto com a API.
# Upload de imagem de capa

`POST /api/v1/admin/conteudos/imagens` envia o campo multipart `arquivo` para o armazenamento configurado e retorna a URL pública da capa. O endpoint exige perfil `ADMIN`.

São aceitos PNG, JPEG e WebP de até 5 MB. A URL retornada deve ser usada no campo `imagemCapa` ao salvar o conteúdo.
