# Catálogo API — Endpoints de Catálogo

Este documento descreve os endpoints responsáveis pelos **catálogos auxiliares** da Concurseiro API.

Os catálogos representam dados estruturais utilizados para classificação das questões e provas do sistema.

Esses dados normalmente mudam pouco e são utilizados principalmente para:

* filtros de busca
* preenchimento de formulários
* categorização de questões
* classificação de provas e conteúdos

---

# Base path

Todos os endpoints públicos deste módulo utilizam o prefixo:

```text
/api/v1/catalogo
```

Além disso, o projeto possui rotas administrativas para manutenção de catálogo sob o prefixo:

```text
/api/v1/admin/catalogo
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

Essas entidades são utilizadas principalmente nos endpoints de **questões** e **provas**.

---

# Endpoints públicos

Os endpoints públicos de catálogo são de leitura e, de acordo com a configuração atual de segurança, podem ser acessados sem autenticação.

---

# GET /api/v1/catalogo/bancas

Lista as bancas cadastradas.

## Exemplo

```http
GET /api/v1/catalogo/bancas
```

## Resposta esperada

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

Observação:
- a estrutura exata do payload depende do DTO utilizado pelo controller
- na API atual, as respostas versionadas podem ser encapsuladas pelo padrão global de resposta da aplicação

---

# GET /api/v1/catalogo/disciplinas

Lista as disciplinas disponíveis.

## Exemplo

```http
GET /api/v1/catalogo/disciplinas
```

## Resposta esperada

```json
[
  {
    "id": 1,
    "nome": "Direito Constitucional"
  }
]
```

---

# GET /api/v1/catalogo/instituicoes

Lista instituições cadastradas.

## Exemplo

```http
GET /api/v1/catalogo/instituicoes
```

## Resposta esperada

```json
[
  {
    "id": 1,
    "nome": "Tribunal Regional Federal"
  }
]
```

---

# GET /api/v1/catalogo/disciplinas/{disciplinaId}/assuntos

Lista os assuntos vinculados a uma disciplina.

## Exemplo

```http
GET /api/v1/catalogo/disciplinas/1/assuntos
```

## Resposta esperada

```json
[
  {
    "id": 1,
    "nome": "Controle de Constitucionalidade"
  }
]
```

Observação:
- na versão atual do projeto, a consulta de assuntos está estruturada por disciplina
- portanto, o endpoint público relevante é a listagem por `disciplinaId`

---

# GET /api/v1/catalogo/assuntos/{assuntoId}/subassuntos

Lista os subassuntos vinculados a um assunto.

## Exemplo

```http
GET /api/v1/catalogo/assuntos/1/subassuntos
```

## Resposta esperada

```json
[
  {
    "id": 1,
    "nome": "ADI"
  }
]
```

Observação:
- na versão atual do projeto, a consulta de subassuntos está estruturada por `assuntoId`

---

# Endpoints administrativos de catálogo

A aplicação também possui endpoints administrativos para criação, atualização e remoção de itens de catálogo.

Essas rotas ficam sob:

```text
/api/v1/admin/catalogo/**
```

Essas operações são restritas a usuários com role `ADMIN`.

Os módulos administrativos existentes atualmente abrangem manutenção de:

* bancas
* disciplinas
* instituições
* assuntos
* subassuntos

A disponibilidade exata de métodos HTTP por entidade depende do controller administrativo correspondente.

---

# Relação com outros módulos

Os dados de catálogo são utilizados principalmente em:

```text
docs/endpoints/questoes.md
docs/endpoints/provas.md
```

Exemplos de uso:

* filtro por disciplina
* filtro por banca
* filtro por assunto
* filtro por instituição
* associação de prova a banca e instituição
* classificação de questão por disciplina e assunto

Esses campos aparecem diretamente nos parâmetros de busca de questões e na criação de provas.

---

# Segurança

Os endpoints públicos de catálogo são **somente leitura** e, na configuração atual, são públicos.

A manutenção de catálogo fica restrita às rotas administrativas:

* públicas para leitura em `/api/v1/catalogo/**`
* protegidas para escrita em `/api/v1/admin/catalogo/**`

A escrita exige usuário autenticado com role `ADMIN`.

---

# Observação

Os campos retornados podem variar conforme os DTOs definidos no módulo `catalogo`.

A estrutura exata deve ser confirmada com os DTOs e controllers do projeto.

Os itens de catálogo são parte importante da normalização de dados da API e devem ser mantidos consistentes com os módulos de questões e provas.
