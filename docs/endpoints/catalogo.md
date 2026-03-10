# Catálogo API — Endpoints de Catálogo

Este documento descreve os endpoints responsáveis pelos **catálogos auxiliares** da Concurseiro API.

Os catálogos representam dados estruturais utilizados para classificação das questões e provas do sistema.

Esses dados normalmente mudam pouco e são utilizados principalmente para:

* filtros de busca
* preenchimento de formulários
* categorização de questões

---

# Base path

Todos os endpoints deste módulo utilizam o prefixo:

```
/api/v1/catalogo
```

---

# Entidades de catálogo

O catálogo da API inclui as seguintes entidades:

| Entidade    | Descrição                          |
| ----------- | ---------------------------------- |
| banca       | organização responsável pela prova |
| disciplina  | área de conhecimento               |
| assunto     | tópico dentro de uma disciplina    |
| subassunto  | subdivisão do assunto              |
| instituicao | órgão que realizou o concurso      |

Essas entidades são utilizadas principalmente nos endpoints de **questões**.

---

# GET /api/v1/catalogo/bancas

Lista todas as bancas cadastradas.

## Exemplo

```
GET /api/v1/catalogo/bancas
```

## Resposta

```json
[
  {
    "id": 1,
    "nome": "FGV"
  },
  {
    "id": 2,
    "nome": "CESPE"
  }
]
```

---

# GET /api/v1/catalogo/disciplinas

Lista todas as disciplinas disponíveis.

## Exemplo

```
GET /api/v1/catalogo/disciplinas
```

## Resposta

```json
[
  {
    "id": 1,
    "nome": "Direito Constitucional"
  }
]
```

---

# GET /api/v1/catalogo/assuntos

Lista assuntos cadastrados.

## Exemplo

```
GET /api/v1/catalogo/assuntos
```

## Resposta

```json
[
  {
    "id": 1,
    "nome": "Controle de Constitucionalidade",
    "disciplinaId": 1
  }
]
```

---

# GET /api/v1/catalogo/subassuntos

Lista subassuntos cadastrados.

## Exemplo

```
GET /api/v1/catalogo/subassuntos
```

## Resposta

```json
[
  {
    "id": 1,
    "nome": "ADI",
    "assuntoId": 1
  }
]
```

---

# GET /api/v1/catalogo/instituicoes

Lista instituições que realizam concursos.

## Exemplo

```
GET /api/v1/catalogo/instituicoes
```

## Resposta

```json
[
  {
    "id": 1,
    "nome": "Tribunal Regional Federal"
  }
]
```

---

# Relação com outros módulos

Os dados de catálogo são utilizados principalmente em:

```
docs/endpoints/questoes.md
```

Exemplos de uso:

* filtro por disciplina
* filtro por banca
* filtro por assunto
* filtro por instituição

Esses campos aparecem diretamente nos parâmetros de busca de questões.

---

# Segurança

Esses endpoints normalmente são **somente leitura**.

Dependendo da configuração da aplicação, podem ser:

* públicos
* acessíveis apenas a usuários autenticados

Criação ou alteração de catálogos, quando existente, normalmente fica restrita a **rotas administrativas**.

---

# Observação

Os campos retornados podem variar conforme os DTOs definidos no módulo `catalogo`. A estrutura exata deve ser confirmada quando os modelos forem documentados em:

```
docs/modelos/
```
