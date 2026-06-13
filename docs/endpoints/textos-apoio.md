# Textos de apoio

## Tipos suportados

O campo `tipo` aceita:

- `TEXTO`
- `CODIGO`
- `TABELA`
- `IMAGEM`

Os tipos `TABELA` e `IMAGEM` exigem `conteudoJson`.

## Upload de imagem

```http
POST /api/v1/admin/textos-apoio/imagens
Content-Type: multipart/form-data
Authorization: Bearer <token-admin>
```

Campos:

| Campo | Obrigatório | Descrição |
| --- | --- | --- |
| `arquivo` | sim | Imagem PNG, JPEG ou WebP de até 5 MB |
| `textoAlternativo` | sim | Descrição acessível de até 500 caracteres |
| `titulo` | não | Título apresentado acima da imagem |

O endpoint envia o arquivo para o Cloudflare R2 e cadastra o texto de apoio como
`IMAGEM`. Se o cadastro no banco falhar, o arquivo enviado é removido.

Exemplo de `conteudoJson` produzido:

```json
{
  "url": "https://assets.example.com/textos-apoio/2026/arquivo.png",
  "alt": "Gráfico com a distribuição dos valores observados",
  "mimeType": "image/png",
  "largura": 900,
  "altura": 420
}
```

O campo textual `conteudo` recebe o mesmo texto alternativo. Assim, clientes
antigos continuam exibindo uma descrição útil mesmo sem suporte visual.

## Configuração

O endpoint só é registrado quando todas estas variáveis estão preenchidas:

- `R2_ENDPOINT`
- `R2_ACCESS_KEY_ID`
- `R2_SECRET_ACCESS_KEY`
- `R2_BUCKET`
- `R2_PUBLIC_BASE_URL`

`R2_REGION` é opcional e usa `auto` por padrão.
