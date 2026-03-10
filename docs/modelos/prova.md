# Modelo de Dados — Prova

Este documento descreve o modelo de **prova** da Concurseiro API e como ele se relaciona com o restante do domínio do sistema.

A prova representa um **conjunto organizado de questões aplicado em um concurso público**, incluindo metadados como banca, instituição e ano.

O objetivo desta documentação é explicar:

* a entidade persistida no banco
* os campos obrigatórios e opcionais
* o relacionamento com questões
* os contratos de entrada e saída
* as regras de negócio do serviço

---

# Localização no projeto

Principais classes relacionadas ao modelo de prova:

```text
src/main/java/br/com/concurseiro/api/prova/model/Prova.java
src/main/java/br/com/concurseiro/api/prova/dto/ProvaRequest.java
src/main/java/br/com/concurseiro/api/prova/dto/ProvaResponse.java
src/main/java/br/com/concurseiro/api/prova/dto/ProvaQuestaoRequest.java
src/main/java/br/com/concurseiro/api/prova/service/ProvaService.java
src/main/java/br/com/concurseiro/api/prova/repository/ProvaRepository.java
src/main/java/br/com/concurseiro/api/prova/controller/ProvaController.java
```

Além disso, o modelo de prova se relaciona com:

```text
Questao
Banca
Instituicao
```

---

# Visão geral do modelo

A entidade **Prova** representa um concurso ou exame específico.

Ela possui metadados básicos:

* identificador interno
* nome da prova
* ano de aplicação
* banca organizadora
* instituição responsável
* data de criação

Além disso, uma prova pode conter **múltiplas questões associadas**.

No modelo atual do projeto, o relacionamento com questões é **indireto**:

* a entidade `Questao` possui o campo `provaId`
* não existe uma coleção `List<Questao>` diretamente dentro da entidade `Prova`

Essa decisão simplifica a modelagem e evita carregamentos pesados de entidades.

---

# Entidade persistida

## Classe

```java
Prova
```

## Tabela

```text
provas
```

---

# Estrutura da entidade

| Campo         | Tipo Java      | Persistido | Obrigatório | Observações                           |
| ------------- | -------------- | ---------- | ----------- | ------------------------------------- |
| id            | Long           | sim        | sim         | chave primária gerada automaticamente |
| nome          | String         | sim        | sim         | nome da prova                         |
| ano           | Integer        | sim        | sim         | ano da prova                          |
| bancaId       | Long           | sim        | sim         | referência à banca                    |
| instituicaoId | Long           | sim        | sim         | referência à instituição              |
| criadoEm      | OffsetDateTime | sim        | sim         | data de criação                       |

---

# Campo por campo

## id

Identificador interno da prova.

Características:

* tipo `Long`
* anotado com `@Id`
* gerado com `GenerationType.IDENTITY`

Esse campo é usado internamente pela aplicação e nas rotas administrativas.

---

## nome

Nome descritivo da prova.

Exemplo:

```text
TRF 1 — Analista Judiciário
```

Características:

* obrigatório
* usado em listagens e identificação da prova

---

## ano

Ano de aplicação da prova.

Características:

* obrigatório
* usado em filtros e consultas

Exemplo:

```text
2024
```

---

## bancaId

Identificador da banca organizadora.

Características:

* obrigatório
* corresponde ao catálogo de bancas

Exemplo:

```text
FGV
CESPE
FCC
```

No modelo atual, a prova armazena apenas o **id da banca**, e não um relacionamento JPA direto.

---

## instituicaoId

Identificador da instituição responsável pelo concurso.

Características:

* obrigatório
* corresponde ao catálogo de instituições

Exemplo:

```text
TRF
TJ
MP
```

Assim como `bancaId`, esse campo armazena apenas o identificador.

---

## criadoEm

Data de criação da prova no sistema.

Características:

* tipo `OffsetDateTime`
* inicializado automaticamente

Esse campo pode ser usado para ordenação em listagens administrativas.

---

# Relação com questões

A prova não possui relacionamento JPA direto com a entidade `Questao`.

Em vez disso, a ligação ocorre através do campo:

```text
Questao.provaId
```

Isso significa que uma prova pode possuir várias questões associadas.

O repositório de questões possui o método:

```java
countByProvaId(Long provaId)
```

Que permite descobrir quantas questões pertencem a uma prova.

Esse modelo reduz acoplamento e evita carregar grandes coleções de questões ao consultar uma prova.

---

# Contratos de entrada

## ProvaRequest

Usado no endpoint:

```http
POST /api/v1/provas
```

### Estrutura

| Campo         | Tipo    | Obrigatório |
| ------------- | ------- | ----------- |
| nome          | String  | sim         |
| ano           | Integer | sim         |
| bancaId       | Long    | sim         |
| instituicaoId | Long    | sim         |

### Exemplo

```json
{
  "nome": "TRF 1 — Analista Judiciário",
  "ano": 2024,
  "bancaId": 1,
  "instituicaoId": 2
}
```

---

## ProvaQuestaoRequest

Usado no endpoint:

```http
POST /api/v1/provas/{provaId}/questoes
```

Esse request adiciona uma questão existente à prova.

### Estrutura

| Campo      | Tipo   | Obrigatório |
| ---------- | ------ | ----------- |
| idQuestion | String | sim         |

### Exemplo

```json
{
  "idQuestion": "Q8F2A4C1D9E7B3F"
}
```

---

# Contrato de saída

## ProvaResponse

DTO usado para retornar dados de prova.

### Estrutura

| Campo         | Tipo           | Descrição                    |
| ------------- | -------------- | ---------------------------- |
| id            | Long           | identificador da prova       |
| nome          | String         | nome da prova                |
| ano           | Integer        | ano da prova                 |
| bancaId       | Long           | identificador da banca       |
| instituicaoId | Long           | identificador da instituição |
| criadoEm      | OffsetDateTime | data de criação              |

### Exemplo

```json
{
  "id": 1,
  "nome": "TRF 1 — Analista Judiciário",
  "ano": 2024,
  "bancaId": 1,
  "instituicaoId": 2,
  "criadoEm": "2026-03-09T10:15:30Z"
}
```

---

# Regras de negócio do serviço

## Criação de prova

Método de serviço:

```java
cadastrar(ProvaRequest request)
```

Fluxo:

1. valida os campos obrigatórios
2. cria nova entidade `Prova`
3. define nome, ano, bancaId e instituicaoId
4. define `criadoEm`
5. salva no banco

---

## Busca de prova

Método:

```java
buscarPorId(Long id)
```

Fluxo:

1. busca prova pelo id
2. se não existir, retorna erro 404

---

## Listagem paginada

Método:

```java
listar(Pageable pageable)
```

Esse método retorna uma lista paginada de provas.

Parâmetros comuns:

```text
page
size
```

---

## Adicionar questão à prova

Método:

```java
cadastrarQuestao(Long provaId, ProvaQuestaoRequest request)
```

Fluxo:

1. busca a prova pelo id
2. busca a questão pelo `idQuestion`
3. define `questao.provaId = provaId`
4. salva a questão atualizada

Esse processo associa a questão à prova.

---

# Repositório

## ProvaRepository

Interface responsável por persistência.

```java
JpaRepository<Prova, Long>
```

Funções principais:

* salvar provas
* buscar prova por id
* listar provas paginadas

---

# Regras importantes para integração

Quem consumir a API deve considerar:

## 1. Provas são identificadas por id numérico

Diferente das questões, que usam `idQuestion`, as provas usam `id` numérico.

## 2. Questões pertencem a provas

Uma questão pode ser associada a uma prova através do campo `provaId`.

## 3. Provas não carregam questões automaticamente

Para listar questões de uma prova, é necessário consultar o endpoint de questões filtrando por `provaId`.

## 4. Catálogo é referenciado por id

Os campos `bancaId` e `instituicaoId` referenciam registros do catálogo.

---

# Exemplo de ciclo de vida de uma prova

## 1. Criação

```json
{
  "nome": "TRF 1 — Analista Judiciário",
  "ano": 2024,
  "bancaId": 1,
  "instituicaoId": 2
}
```

## 2. Inserção de questão

```http
POST /api/v1/provas/1/questoes
```

```json
{
  "idQuestion": "Q8F2A4C1D9E7B3F"
}
```

## 3. Consulta da prova

```http
GET /api/v1/provas/1
```

---

# Resumo do modelo

O modelo de prova da Concurseiro API é simples e eficiente.

Ele separa claramente:

* metadados da prova
* catálogo de instituições e bancas
* associação de questões

Essa modelagem evita dependências pesadas entre entidades e mantém o sistema mais performático e escalável.
