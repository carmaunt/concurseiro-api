# Modelo de Dados — Comentário

Este documento descreve o modelo de **comentário** da Concurseiro API e sua função dentro do domínio do sistema.

Comentários permitem que usuários registrem observações, explicações ou discussões relacionadas principalmente às **questões de concursos** armazenadas na plataforma.

O objetivo deste documento é explicar:

* a entidade persistida no banco
* os campos obrigatórios e opcionais
* a relação com questões
* os contratos de entrada e saída
* as regras de negócio do serviço

---

# Localização no projeto

Principais classes relacionadas ao modelo de comentário:

```text
src/main/java/br/com/concurseiro/api/comentario/model/Comentario.java
src/main/java/br/com/concurseiro/api/comentario/dto/ComentarioRequest.java
src/main/java/br/com/concurseiro/api/comentario/dto/ComentarioResponse.java
src/main/java/br/com/concurseiro/api/comentario/service/ComentarioService.java
src/main/java/br/com/concurseiro/api/comentario/repository/ComentarioRepository.java
src/main/java/br/com/concurseiro/api/comentario/controller/ComentarioController.java
```

---

# Visão geral do modelo

A entidade **Comentario** representa uma anotação feita por um usuário sobre uma questão.

Cada comentário contém:

* identificador interno
* texto do comentário
* referência da questão
* data de criação

O modelo é propositalmente simples para permitir inserção rápida de comentários e leitura eficiente.

---

# Entidade persistida

## Classe

```java
Comentario
```

## Tabela

```text
comentarios
```

---

# Estrutura da entidade

| Campo     | Tipo Java      | Persistido | Obrigatório | Observações                           |
| --------- | -------------- | ---------- | ----------- | ------------------------------------- |
| id        | Long           | sim        | sim         | chave primária gerada automaticamente |
| texto     | String         | sim        | sim         | conteúdo do comentário                |
| questaoId | String         | sim        | sim         | identificador funcional da questão    |
| criadoEm  | OffsetDateTime | sim        | sim         | data de criação                       |

---

# Campo por campo

## id

Identificador interno do comentário.

Características:

* tipo `Long`
* anotado com `@Id`
* gerado com `GenerationType.IDENTITY`

Esse campo é utilizado internamente para operações como remoção do comentário.

---

## texto

Conteúdo textual do comentário.

Características:

* obrigatório
* armazenado como texto longo

Esse campo contém a explicação, observação ou discussão registrada pelo usuário.

Exemplo:

```text
Essa questão costuma aparecer em provas da FGV.
```

---

## questaoId

Identificador funcional da questão associada.

Características:

* obrigatório
* corresponde ao campo `idQuestion` da entidade `Questao`

Exemplo:

```text
Q8F2A4C1D9E7B3F
```

Esse campo cria o vínculo lógico entre comentário e questão.

Diferente de outras entidades do sistema, esse relacionamento é **indireto** e não utiliza `@ManyToOne`.

Essa escolha reduz acoplamento e evita carregamentos desnecessários de entidades.

---

## criadoEm

Data de criação do comentário.

Características:

* tipo `OffsetDateTime`
* preenchido automaticamente no momento da criação

Esse campo pode ser utilizado para ordenação cronológica de comentários.

---

# Relação com questões

Cada comentário pertence a **uma única questão**.

O relacionamento é estabelecido através do campo:

```text
questaoId
```

Esse valor corresponde ao identificador público da questão.

Fluxo típico:

1. usuário visualiza uma questão
2. usuário registra um comentário
3. comentário é armazenado com `questaoId`
4. comentários podem ser consultados posteriormente

---

# Contrato de entrada

## ComentarioRequest

Usado no endpoint:

```http
POST /api/v1/comentarios
```

### Estrutura

| Campo     | Tipo   | Obrigatório |
| --------- | ------ | ----------- |
| texto     | String | sim         |
| questaoId | String | sim         |

### Exemplo

```json
{
  "texto": "Essa questão costuma aparecer em provas da FGV",
  "questaoId": "Q8F2A4C1D9E7B3F"
}
```

---

# Contrato de saída

## ComentarioResponse

DTO utilizado para retornar comentários ao cliente.

### Estrutura

| Campo     | Tipo           | Descrição                   |
| --------- | -------------- | --------------------------- |
| id        | Long           | identificador do comentário |
| texto     | String         | conteúdo do comentário      |
| questaoId | String         | identificador da questão    |
| criadoEm  | OffsetDateTime | data de criação             |

### Exemplo

```json
{
  "id": 1,
  "texto": "Essa questão costuma aparecer em provas da FGV",
  "questaoId": "Q8F2A4C1D9E7B3F",
  "criadoEm": "2026-03-09T10:15:30Z"
}
```

---

# Regras de negócio do serviço

## Criação de comentário

Método de serviço:

```java
criarComentario(ComentarioRequest request)
```

Fluxo:

1. recebe o request
2. valida os campos obrigatórios
3. cria nova entidade `Comentario`
4. define `texto`
5. define `questaoId`
6. define `criadoEm`
7. salva no banco

---

## Listagem de comentários

Método:

```java
listar(Pageable pageable)
```

Retorna comentários de forma paginada.

Parâmetros comuns:

```text
page
size
```

---

## Comentários por questão

Método:

```java
listarPorQuestao(String questaoId)
```

Fluxo:

1. recebe o identificador da questão
2. busca todos os comentários associados
3. retorna a lista

---

## Exclusão de comentário

Método:

```java
excluir(Long id)
```

Fluxo:

1. busca comentário pelo id
2. se não existir, retorna erro 404
3. remove o registro

---

# Repositório

## ComentarioRepository

Interface responsável pelo acesso ao banco de dados.

```java
JpaRepository<Comentario, Long>
```

Funções principais:

* salvar comentários
* buscar comentários por questão
* listar comentários
* excluir comentários

---

# Regras importantes para integração

Quem consumir a API deve considerar:

## 1. Comentários sempre pertencem a uma questão

Não existem comentários independentes.

## 2. O vínculo com a questão é feito pelo idQuestion

O campo `questaoId` deve receber o identificador funcional da questão.

## 3. Comentários são independentes de usuários

O modelo atual não armazena o usuário autor do comentário.

Isso simplifica o domínio, mas significa que a API não mantém histórico de autoria.

## 4. Comentários podem crescer rapidamente

Em aplicações com grande volume de uso, esse módulo pode se tornar uma fonte significativa de dados.

Paginação e filtragem devem ser usadas ao consumir os endpoints.

---

# Exemplo de ciclo de vida do comentário

## 1. Criação

```json
{
  "texto": "Essa questão costuma aparecer em provas da FGV",
  "questaoId": "Q8F2A4C1D9E7B3F"
}
```

## 2. Consulta de comentários

```http
GET /api/v1/comentarios/questao/Q8F2A4C1D9E7B3F
```

## 3. Remoção

```http
DELETE /api/v1/comentarios/1
```

---

# Resumo do modelo

O modelo de comentário da Concurseiro API é propositalmente simples e focado em performance.

Ele separa claramente:

* conteúdo textual
* vínculo com questão
* data de criação

Essa simplicidade permite registrar discussões sobre questões sem introduzir complexidade excessiva no domínio.
