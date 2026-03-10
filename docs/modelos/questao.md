# Modelo de Dados — Questão

Este documento descreve em profundidade o modelo de **questão** da Concurseiro API com base no código real do projeto.

O objetivo deste arquivo é documentar:

* a entidade persistida no banco de dados
* os campos obrigatórios e opcionais
* os vínculos com catálogo
* os contratos de entrada e saída
* as regras de validação
* o fluxo de criação, atualização, busca e exclusão
* as regras de filtro e ordenação

---

# Localização no projeto

Principais classes relacionadas ao modelo de questão:

```text
src/main/java/br/com/concurseiro/api/questoes/model/Questao.java
src/main/java/br/com/concurseiro/api/questoes/dto/QuestaoRequest.java
src/main/java/br/com/concurseiro/api/questoes/dto/QuestaoResponse.java
src/main/java/br/com/concurseiro/api/questoes/service/QuestaoService.java
src/main/java/br/com/concurseiro/api/questoes/service/QuestaoValidationHelper.java
src/main/java/br/com/concurseiro/api/questoes/repository/QuestaoRepository.java
src/main/java/br/com/concurseiro/api/questoes/spec/QuestaoSpecifications.java
```

Além disso, o modelo de questão se relaciona com o catálogo:

```text
src/main/java/br/com/concurseiro/api/catalogo/disciplina/model/Disciplina.java
src/main/java/br/com/concurseiro/api/catalogo/assunto/model/Assunto.java
src/main/java/br/com/concurseiro/api/catalogo/banca/model/Banca.java
src/main/java/br/com/concurseiro/api/catalogo/instituicao/model/Instituicao.java
```

---

# Visão geral do modelo

A **questão** é a entidade central do sistema.

Ela representa uma questão de concurso com:

* identificador funcional próprio
* enunciado e conteúdo da questão
* alternativas
* classificação por disciplina, assunto, banca e instituição
* metadados como ano, cargo, nível e modalidade
* gabarito
* vínculo opcional com prova
* data de criação
* campo otimizado para busca textual

A modelagem atual adota um formato híbrido.

Isso significa que a questão mantém ao mesmo tempo:

1. **campos textuais legados**, como `disciplina`, `assunto`, `banca` e `instituicao`
2. **vínculos novos com catálogo**, como `disciplinaCatalogo`, `assuntoCatalogo`, `bancaCatalogo` e `instituicaoCatalogo`

Esse desenho não é acidente; ele existe para permitir uma **migração gradual** do modelo antigo baseado em texto para um modelo normalizado baseado em entidades de catálogo.

---

# Entidade persistida

## Classe

```java
Questao
```

## Tabela

```text
questoes
```

## Índices declarados

A tabela possui índices explícitos para melhorar consulta:

| Índice                  | Campo       |
| ----------------------- | ----------- |
| idx_questao_ano         | ano         |
| idx_questao_id_question | id_question |
| idx_questao_texto_busca | textoBusca  |

Esses índices mostram com bastante clareza onde o sistema espera volume e consulta frequente.

---

# Estrutura da entidade

| Campo               | Tipo Java      | Persistido | Obrigatório | Observações                                      |
| ------------------- | -------------- | ---------- | ----------- | ------------------------------------------------ |
| id                  | Long           | sim        | sim         | chave primária interna                           |
| idQuestion          | String         | sim        | sim         | identificador funcional único, até 16 caracteres |
| enunciado           | String         | sim        | sim         | texto livre longo, armazenado como LOB           |
| questao             | String         | sim        | sim         | conteúdo da questão, armazenado como LOB         |
| alternativas        | String         | sim        | sim         | alternativas em texto livre, armazenado como LOB |
| disciplina          | String         | sim        | sim         | campo textual legado, até 160                    |
| assunto             | String         | sim        | sim         | campo textual legado, até 200                    |
| banca               | String         | sim        | sim         | campo textual legado, até 160                    |
| instituicao         | String         | sim        | sim         | campo textual legado, até 200                    |
| ano                 | Integer        | sim        | sim         | ano da questão                                   |
| cargo               | String         | sim        | sim         | cargo relacionado, até 160                       |
| nivel               | String         | sim        | sim         | nível da prova, até 80                           |
| modalidade          | String         | sim        | sim         | modalidade da questão, até 40                    |
| gabarito            | String         | sim        | não         | até 8 caracteres na entidade                     |
| provaId             | Long           | sim        | não         | vínculo opcional com prova                       |
| criadoEm            | OffsetDateTime | sim        | sim         | data/hora de criação                             |
| disciplinaCatalogo  | Disciplina     | sim        | não         | vínculo gradual com catálogo                     |
| assuntoCatalogo     | Assunto        | sim        | não         | vínculo gradual com catálogo                     |
| bancaCatalogo       | Banca          | sim        | não         | vínculo gradual com catálogo                     |
| instituicaoCatalogo | Instituicao    | sim        | não         | vínculo gradual com catálogo                     |
| textoBusca          | String         | sim        | não         | campo derivado e otimizado para pesquisa textual |

---

# Campo por campo

## id

Identificador interno do banco.

Características:

* tipo `Long`
* anotado com `@Id`
* gerado com `GenerationType.IDENTITY`

Esse campo não é o identificador funcional principal da API.

---

## idQuestion

Identificador funcional e público da questão.

Características:

* coluna `id_question`
* obrigatório
* único
* tamanho máximo `16`

Esse é o identificador usado nas rotas da API, por exemplo:

```http
GET /api/v1/questoes/{idQuestion}
```

### Regra de geração

O serviço gera esse valor automaticamente usando `QuestaoValidationHelper.gerarIdQuestion()`.

Formato gerado:

* prefixo fixo `Q`
* seguido dos primeiros 15 caracteres hexadecimais de um UUID

Na prática, o resultado fica parecido com:

```text
Q8F2A4C1D9E7B3F
```

Ou seja: o domínio usa um identificador público próprio, e não expõe o `id` numérico interno. Escolha sensata. Menos acoplamento, menos dor de cabeça.

---

## enunciado

Texto introdutório da questão.

Características:

* obrigatório
* armazenado como `@Lob`
* validado no request com máximo de `10000` caracteres

Esse campo participa da composição de `textoBusca`.

---

## questao

Corpo principal da questão.

Características:

* obrigatório
* armazenado como `@Lob`
* validado com máximo de `10000` caracteres

Esse campo também participa da busca textual.

---

## alternativas

Texto livre contendo as alternativas.

Características:

* obrigatório
* armazenado como `@Lob`
* validado com máximo de `10000` caracteres

Além de armazenar as alternativas em si, esse campo também influencia a normalização da modalidade.

Quando a modalidade vier como “múltipla escolha” sem especificar A-D ou A-E, o sistema tenta inferir a forma olhando se o texto das alternativas contém `E)`.

---

## disciplina

n
Campo textual legado da disciplina.

Características:

* obrigatório
* máximo `160`

Quando `disciplinaId` é enviado no request, esse campo deixa de ser fonte de verdade e passa a ser **derivado do catálogo**.

Ou seja:

* catálogo define a disciplina real
* o nome textual é copiado de `disciplinaCatalogo.getNome()`

Se `disciplinaId` não vier, a API usa o valor textual informado em `disciplina`.

---

## assunto

Campo textual legado do assunto.

Características:

* obrigatório
* máximo `200`

Regra semelhante à disciplina:

* se `assuntoId` vier, o texto passa a ser derivado do catálogo
* se não vier, o texto informado diretamente é usado

---

## banca

Campo textual legado da banca.

Características:

* obrigatório
* máximo `160`

Regra:

* se `bancaId` vier, o texto é derivado do catálogo
* se não vier, o texto informado no request é usado

---

## instituicao

Campo textual legado da instituição.

Características:

* obrigatório
* máximo `200`

Mas aqui existe uma pegadinha importante do domínio atual:

### Regra de criação

No cadastro de questão, `instituicaoId` é **obrigatório**.

Ou seja, embora o request ainda tenha o campo textual `instituicao`, a criação de nova questão exige que a instituição exista no catálogo e seja enviada por id.

No cadastro:

* se `instituicaoId` estiver ausente, o serviço retorna `400 Bad Request`
* se `instituicaoId` estiver presente, o nome textual é derivado do catálogo

Na atualização, a regra é mais flexível:

* se `instituicaoId` vier, usa catálogo
* se não vier, mantém o valor textual enviado em `instituicao`

Essa assimetria precisa ficar documentada porque ela não é óbvia à primeira vista.

---

## ano

Ano da questão.

Características:

* obrigatório
* validado com `@Min(1900)`
* validado com `@Max(2100)`

---

## cargo

Cargo relacionado à questão.

Características:

* obrigatório
* máximo `160`

Esse campo também pode ser usado como filtro nas buscas.

---

## nivel

Nível da questão ou da prova associada.

Características:

* obrigatório
* máximo `80`

Esse campo também é filtrável.

---

## modalidade

Representa o tipo de estrutura da questão.

Características:

* obrigatório
* máximo `40` na entidade
* no request deve obedecer um padrão validado

Valores canônicos do domínio:

| Valor        | Significado                               |
| ------------ | ----------------------------------------- |
| A_E          | múltipla escolha com alternativas A até E |
| A_D          | múltipla escolha com alternativas A até D |
| CERTO_ERRADO | questão de certo ou errado                |

### Normalização de entrada

O helper aceita várias formas equivalentes e converte para o formato canônico.

Exemplos aceitos:

* `MÚLTIPLA ESCOLHA A-E`
* `MULTIPLA ESCOLHA A-E`
* `MÚLTIPLA ESCOLHA A_E`
* `MULTIPLA ESCOLHA A_E`
* `MÚLTIPLA ESCOLHA A-D`
* `MULTIPLA ESCOLHA A-D`
* `MÚLTIPLA ESCOLHA`
* `MULTIPLA ESCOLHA`
* `CERTO E ERRADO`
* `CERTO/ERRADO`
* `A_E`
* `A_D`
* `CERTO_ERRADO`

Se vier apenas `MÚLTIPLA ESCOLHA` ou `MULTIPLA ESCOLHA`, o sistema infere:

* `A_E` se o texto de alternativas contiver `E)`
* `A_D` caso contrário

Se o valor não puder ser normalizado, a API retorna erro de validação.

---

## gabarito

Resposta correta da questão.

Características:

* obrigatório no request
* armazenado na entidade com até `8` caracteres
* validado de acordo com a modalidade

### Regras por modalidade

#### Para `A_E`

Aceitos apenas:

* `A`
* `B`
* `C`
* `D`
* `E`

#### Para `A_D`

Aceitos apenas:

* `A`
* `B`
* `C`
* `D`

#### Para `CERTO_ERRADO`

Aceitos:

* `C`
* `E`
* `CERTO`
* `ERRADO`

### Normalização

Quando a modalidade for `CERTO_ERRADO`, o helper converte:

* `CERTO` → `C`
* `ERRADO` → `E`

Assim, o valor persistido fica normalizado.

---

## provaId

Identificador da prova associada.

Características:

* opcional
* armazenado como `Long`
* não é um relacionamento JPA completo

O projeto escolheu armazenar o vínculo com prova apenas como id, e não como `@ManyToOne`.

Isso sugere uma modelagem mais simples para esse relacionamento neste momento.

O repositório inclusive fornece:

```java
countByProvaId(Long provaId)
```

O que indica uso direto desse campo para operações ligadas à prova.

---

## criadoEm

Data de criação da questão.

Características:

* tipo `OffsetDateTime`
* obrigatório
* inicializado com `OffsetDateTime.now()`

Também é um campo permitido para ordenação nas buscas.

---

## textoBusca

Campo derivado para pesquisa textual otimizada.

Características:

* preenchido automaticamente em `@PrePersist` e `@PreUpdate`
* guarda texto normalizado em caixa alta e sem acentos
* tamanho máximo `20000`

### Composição

Esse campo é montado a partir de:

* `enunciado`
* `questao`
* `assunto`

### Normalização aplicada

O processo:

1. remove acentos
2. compacta espaços duplicados
3. faz trim
4. converte tudo para maiúsculas

Isso permite buscas textuais mais tolerantes a variações de acentuação e caixa.

Exemplo conceitual:

```text
"Ação Direta de Inconstitucionalidade"
```

vira algo próximo de:

```text
"ACAO DIRETA DE INCONSTITUCIONALIDADE"
```

---

# Relacionamentos com catálogo

A questão mantém vínculos opcionais com entidades de catálogo.

## disciplinaCatalogo

Relacionamento:

```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "disciplina_id")
```

Tipo:

```java
Disciplina
```

A entidade `Disciplina` possui:

* `id`
* `nome`
* `ativo`
* `criadoEm`

---

## assuntoCatalogo

Relacionamento:

```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "assunto_id")
```

Tipo:

```java
Assunto
```

A entidade `Assunto` possui:

* `id`
* `disciplina`
* `nome`
* `ativo`
* `criadoEm`

---

## bancaCatalogo

Relacionamento:

```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "banca_id")
```

Tipo:

```java
Banca
```

A entidade `Banca` possui:

* `id`
* `nome`

---

## instituicaoCatalogo

Relacionamento:

```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "instituicao_id")
```

Tipo:

```java
Instituicao
```

A entidade `Instituicao` possui:

* `id`
* `nome`

---

# Contrato de entrada

## QuestaoRequest

Usado nos endpoints de criação e atualização.

### Estrutura

| Campo         | Tipo    | Obrigatório                                         | Regras                                                 |
| ------------- | ------- | --------------------------------------------------- | ------------------------------------------------------ |
| enunciado     | String  | sim                                                 | `@NotBlank`, máximo 10000                              |
| questao       | String  | sim                                                 | `@NotBlank`, máximo 10000                              |
| alternativas  | String  | sim                                                 | `@NotBlank`, máximo 10000                              |
| disciplina    | String  | sim                                                 | `@NotBlank`, máximo 160                                |
| assunto       | String  | sim                                                 | `@NotBlank`, máximo 200                                |
| banca         | String  | sim                                                 | `@NotBlank`, máximo 160                                |
| instituicao   | String  | sim                                                 | `@NotBlank`, máximo 200                                |
| disciplinaId  | Long    | não                                                 | sem validação Bean Validation                          |
| assuntoId     | Long    | não                                                 | sem validação Bean Validation                          |
| bancaId       | Long    | não                                                 | sem validação Bean Validation                          |
| instituicaoId | Long    | não no DTO, mas obrigatório no cadastro via service | sem validação Bean Validation                          |
| ano           | Integer | sim                                                 | `@NotNull`, entre 1900 e 2100                          |
| cargo         | String  | sim                                                 | `@NotBlank`, máximo 160                                |
| nivel         | String  | sim                                                 | `@NotBlank`, máximo 80                                 |
| modalidade    | String  | sim                                                 | `@NotBlank`, regex para `A_E`, `A_D` ou `CERTO_ERRADO` |
| gabarito      | String  | sim                                                 | `@NotBlank`                                            |

### Observação importante sobre `modalidade`

Aqui mora um pequeno gremlin arquitetural.

O DTO valida `modalidade` com regex aceitando apenas:

* `A_E`
* `A_D`
* `CERTO_ERRADO`

Mas o helper de serviço aceita também formas humanas como `MULTIPLA ESCOLHA` e `CERTO E ERRADO`.

Na prática, isso significa que:

* no fluxo HTTP normal, o Bean Validation tende a barrar valores fora do padrão canônico
* o helper existe como camada extra de normalização e proteção lógica

Para consumidores da API, a recomendação oficial deve ser sempre enviar os valores canônicos.

### Exemplo recomendado

```json
{
  "enunciado": "Considere as afirmativas abaixo.",
  "questao": "Assinale a alternativa correta.",
  "alternativas": "A) ...\nB) ...\nC) ...\nD) ...\nE) ...",
  "disciplina": "Direito Constitucional",
  "assunto": "Controle de Constitucionalidade",
  "banca": "FGV",
  "instituicao": "TRF 1",
  "disciplinaId": 1,
  "assuntoId": 2,
  "bancaId": 3,
  "instituicaoId": 4,
  "ano": 2024,
  "cargo": "Analista Judiciário",
  "nivel": "SUPERIOR",
  "modalidade": "A_E",
  "gabarito": "C"
}
```

---

# Contrato de saída

## QuestaoResponse

Esse DTO é usado para retornar a questão para o cliente.

### Estrutura

| Campo         | Tipo           | Descrição                                    |
| ------------- | -------------- | -------------------------------------------- |
| idQuestion    | String         | identificador funcional público              |
| enunciado     | String         | enunciado da questão                         |
| questao       | String         | corpo da questão                             |
| alternativas  | String         | alternativas                                 |
| disciplina    | String         | nome da disciplina                           |
| disciplinaId  | Long           | id da disciplina do catálogo, quando houver  |
| assunto       | String         | nome do assunto                              |
| assuntoId     | Long           | id do assunto do catálogo, quando houver     |
| banca         | String         | nome da banca                                |
| bancaId       | Long           | id da banca do catálogo, quando houver       |
| instituicao   | String         | nome da instituição                          |
| instituicaoId | Long           | id da instituição do catálogo, quando houver |
| ano           | Integer        | ano                                          |
| cargo         | String         | cargo                                        |
| nivel         | String         | nível                                        |
| modalidade    | String         | modalidade canônica                          |
| gabarito      | String         | gabarito normalizado                         |
| provaId       | Long           | id da prova associada, quando houver         |
| criadoEm      | OffsetDateTime | data de criação                              |

### Regra de serialização

O método `fromEntity(...)` faz uma conversão híbrida:

* retorna sempre os campos textuais
* retorna também os ids de catálogo quando os vínculos existirem

Isso é útil porque o cliente consegue:

1. exibir os nomes diretamente
2. manter referência estruturada para formulários e filtros

### Exemplo

```json
{
  "idQuestion": "Q8F2A4C1D9E7B3F",
  "enunciado": "Considere as afirmativas abaixo.",
  "questao": "Assinale a alternativa correta.",
  "alternativas": "A) ...\nB) ...\nC) ...\nD) ...\nE) ...",
  "disciplina": "Direito Constitucional",
  "disciplinaId": 1,
  "assunto": "Controle de Constitucionalidade",
  "assuntoId": 2,
  "banca": "FGV",
  "bancaId": 3,
  "instituicao": "TRF 1",
  "instituicaoId": 4,
  "ano": 2024,
  "cargo": "Analista Judiciário",
  "nivel": "SUPERIOR",
  "modalidade": "A_E",
  "gabarito": "C",
  "provaId": 10,
  "criadoEm": "2026-03-09T10:15:30Z"
}
```

---

# Regras de negócio do serviço

## Cadastro de questão

Método:

```java
cadastrar(QuestaoRequest request)
```

Fluxo de negócio:

1. recebe o request
2. normaliza a modalidade
3. valida compatibilidade entre modalidade e gabarito
4. normaliza o gabarito quando necessário
5. gera `idQuestion`
6. copia os campos textuais principais
7. resolve vínculos de catálogo quando ids forem enviados
8. exige `instituicaoId` no cadastro
9. salva a entidade

### Erros de negócio possíveis

* modalidade inválida
* gabarito incompatível com modalidade
* disciplina do catálogo não encontrada
* assunto do catálogo não encontrado
* banca do catálogo não encontrada
* instituição do catálogo não encontrada
* `instituicaoId` ausente no cadastro

---

## Atualização de questão

Método:

```java
atualizar(String idQuestion, QuestaoRequest request)
```

Fluxo de negócio:

1. busca a questão por `idQuestion`
2. se não existir, retorna 404
3. revalida modalidade e gabarito
4. atualiza campos textuais
5. atualiza catálogo quando ids forem enviados
6. quando ids não forem enviados, usa os textos do request
7. salva a entidade atualizada

### Diferença importante em relação ao cadastro

No update, `instituicaoId` não é obrigatório.

Ou seja:

* cadastro exige catálogo de instituição
* atualização aceita tanto catálogo quanto texto simples

---

## Exclusão de questão

Método:

```java
excluir(String idQuestion)
```

Fluxo:

1. busca por `idQuestion`
2. se não existir, retorna 404
3. remove a entidade

---

## Busca de questão por id funcional

Método:

```java
buscarPorIdQuestion(String idQuestion)
```

Fluxo:

1. busca por `idQuestion`
2. retorna a entidade
3. se não existir, lança `404 Not Found`

---

# Repositório

## QuestaoRepository

Responsável por persistência e busca.

Assinatura funcional observada:

```java
JpaRepository
JpaSpecificationExecutor
```

Métodos documentados:

```java
Optional findByIdQuestion(String idQuestion)
long countByProvaId(Long provaId)
```

### Papel do repositório

* salvar questão
* atualizar questão
* excluir questão
* buscar por `idQuestion`
* listar com especificações dinâmicas
* contar quantas questões existem em uma prova

---

# Busca filtrada e paginação

## Método de serviço

```java
listarFiltradoPaginado(...)
```

### Filtros aceitos

| Filtro        | Tipo    | Observação                                |
| ------------- | ------- | ----------------------------------------- |
| texto         | String  | busca textual normalizada em `textoBusca` |
| disciplina    | String  | filtro textual legado                     |
| disciplinaId  | Long    | filtro preferencial por catálogo          |
| assunto       | String  | filtro textual legado                     |
| assuntoId     | Long    | filtro preferencial por catálogo          |
| banca         | String  | filtro textual legado                     |
| bancaId       | Long    | filtro preferencial por catálogo          |
| instituicao   | String  | filtro textual legado                     |
| instituicaoId | Long    | filtro preferencial por catálogo          |
| ano           | Integer | igualdade exata                           |
| cargo         | String  | igualdade textual ignorando caixa         |
| nivel         | String  | igualdade textual ignorando caixa         |
| modalidade    | String  | igualdade textual em caixa alta           |

### Regra de preferência por ids

Quando um id de catálogo é enviado, ele tem precedência sobre o filtro textual correspondente.

Exemplo:

* se `disciplinaId` vier, o filtro por `disciplina` textual é ignorado na composição da specification
* o mesmo vale para `assuntoId`, `bancaId` e `instituicaoId`

O serviço inclusive registra warnings em log quando filtros textuais são usados sem os ids, incentivando migração para o catálogo.

---

# Specifications

## Busca textual

A specification `textoContains(...)`:

* remove acentos do termo pesquisado
* normaliza espaços
* converte para maiúsculas
* executa `like` sobre `textoBusca`

Isso significa que a busca é tolerante a acentos e diferenças de caixa.

---

## Filtros textuais exatos

As specifications textuais usam igualdade com normalização de caixa.

Exemplos:

* `disciplinaEquals`
* `assuntoEquals`
* `bancaEquals`
* `instituicaoEquals`
* `cargoEquals`
* `nivelEquals`

Elas comparam `lower(campo)` com `lower(valor informado)`.

---

## Filtros por id de catálogo

As specifications por id navegam pelo relacionamento JPA:

* `disciplinaCatalogo.id`
* `assuntoCatalogo.id`
* `bancaCatalogo.id`
* `instituicaoCatalogo.id`

Isso mostra que o backend já trata o catálogo como fonte de verdade preferencial.

---

# Ordenação permitida

A ordenação não é livre. O serviço limita explicitamente os campos aceitos.

## Campos permitidos

| Campo       | Permitido |
| ----------- | --------- |
| ano         | sim       |
| criadoEm    | sim       |
| disciplina  | sim       |
| banca       | sim       |
| instituicao | sim       |

Se o cliente enviar outro campo em `sort`, a API retorna erro com mensagem indicando os campos válidos.

### Formato esperado

```text
sort=campo,asc
sort=campo,desc
```

Exemplos válidos:

```text
sort=ano,desc
sort=criadoEm,asc
sort=banca,desc
```

---

# Regras importantes para integração

Quem consumir a API deve considerar estas regras como oficiais:

## 1. O identificador público da questão é `idQuestion`

Não use o `id` interno do banco em integrações externas.

## 2. O cadastro exige `instituicaoId`

Mesmo que exista campo textual, nova questão precisa de instituição do catálogo.

## 3. Modalidade deve ser enviada no formato canônico

Use preferencialmente:

* `A_E`
* `A_D`
* `CERTO_ERRADO`

## 4. Gabarito depende da modalidade

O backend valida a consistência entre ambos.

## 5. O modelo ainda é híbrido

A API mantém texto e catálogo ao mesmo tempo.

## 6. Busca textual usa campo derivado normalizado

Isso melhora desempenho e tolerância a acentos.

## 7. Ordenação é restrita

Não envie qualquer nome de campo em `sort`. Apenas os permitidos.

## 8. Filtros por id são preferíveis aos filtros textuais

O próprio backend já trata ids como fonte de verdade preferencial.

---

# Exemplo consolidado do ciclo de vida da questão

## 1. Criação

```json
{
  "enunciado": "Considere as afirmativas abaixo.",
  "questao": "Assinale a alternativa correta.",
  "alternativas": "A) ...\nB) ...\nC) ...\nD) ...\nE) ...",
  "disciplina": "Direito Constitucional",
  "assunto": "Controle de Constitucionalidade",
  "banca": "FGV",
  "instituicao": "TRF 1",
  "disciplinaId": 1,
  "assuntoId": 2,
  "bancaId": 3,
  "instituicaoId": 4,
  "ano": 2024,
  "cargo": "Analista Judiciário",
  "nivel": "SUPERIOR",
  "modalidade": "A_E",
  "gabarito": "C"
}
```

Resultado esperado no domínio:

* `idQuestion` gerado automaticamente
* `modalidade` normalizada
* `gabarito` validado
* textos de catálogo sincronizados com os ids
* `textoBusca` recalculado

## 2. Busca por id funcional

```http
GET /api/v1/questoes/Q8F2A4C1D9E7B3F
```

## 3. Busca paginada com filtro

```http
GET /api/v1/questoes?disciplinaId=1&ano=2024&sort=criadoEm,desc&page=0&size=10
```

## 4. Atualização

A atualização reaplica todas as regras de modalidade, gabarito e catálogo.

## 5. Exclusão

A exclusão remove a questão usando `idQuestion`.

---

# Resumo do modelo

O modelo de questão da Concurseiro API é o centro do sistema e já revela uma transição arquitetural importante.

Ele combina:

* persistência textual legada
* normalização progressiva via catálogo
* identificador funcional próprio
* busca textual otimizada
* filtros dinâmicos via Specification
* validação forte de modalidade e gabarito

Em termos práticos, é um modelo robusto e já preparado para migração controlada do mundo “texto solto” para o mundo “catálogo estruturado”, que é exatamente onde sistemas grandes deixam de ser uma selva e viram civilização.
