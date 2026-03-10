# Paginação — Concurseiro API

Este documento descreve como funciona o sistema de **paginação de resultados** na Concurseiro API.

A API utiliza o padrão de paginação do **Spring Data**, permitindo dividir grandes conjuntos de dados em páginas menores e mais eficientes para consumo por aplicações cliente.

---

# Visão geral

Endpoints que retornam listas de recursos normalmente utilizam paginação.

Isso evita:

* retorno de grandes volumes de dados
* consumo excessivo de memória
* lentidão nas respostas

A paginação é controlada por **query parameters** enviados na requisição.

---

# Parâmetros de paginação

Os seguintes parâmetros podem ser utilizados:

| Parâmetro | Tipo   | Descrição                      |
| --------- | ------ | ------------------------------ |
| page      | number | número da página (começa em 0) |
| size      | number | quantidade de itens por página |
| sort      | string | campo usado para ordenação     |

---

# Exemplo de requisição

```
GET /api/v1/questoes?page=0&size=10
```

Neste exemplo:

* página solicitada: **0**
* quantidade de itens: **10**

---

# Ordenação de resultados

A ordenação pode ser feita com o parâmetro `sort`.

Formato:

```
sort=campo,ordem
```

Onde:

* `campo` → campo da entidade
* `ordem` → `asc` ou `desc`

Exemplo:

```
GET /api/v1/questoes?page=0&size=10&sort=id,desc
```

---

# Estrutura de resposta paginada

Respostas paginadas geralmente seguem uma estrutura semelhante a:

```json
{
  "content": [
    {
      "id": 1,
      "titulo": "Questão exemplo"
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 120,
  "totalPages": 12
}
```

---

# Campos da resposta

| Campo         | Descrição                      |
| ------------- | ------------------------------ |
| content       | lista de itens retornados      |
| page          | página atual                   |
| size          | quantidade de itens por página |
| totalElements | total de registros disponíveis |
| totalPages    | total de páginas               |

---

# Exemplo completo

Requisição:

```
GET /api/v1/questoes?page=1&size=5
```

Resposta:

```json
{
  "content": [
    { "id": 6, "titulo": "Questão A" },
    { "id": 7, "titulo": "Questão B" },
    { "id": 8, "titulo": "Questão C" },
    { "id": 9, "titulo": "Questão D" },
    { "id": 10, "titulo": "Questão E" }
  ],
  "page": 1,
  "size": 5,
  "totalElements": 120,
  "totalPages": 24
}
```

---

# Boas práticas

Para consumir endpoints paginados corretamente:

* sempre controlar `page` e `size`
* evitar páginas muito grandes
* usar ordenação quando necessário
* utilizar `totalPages` para navegação entre páginas

---

# Resumo

A paginação permite que a Concurseiro API retorne grandes conjuntos de dados de forma eficiente, oferecendo controle ao cliente sobre quantos registros devem ser retornados por requisição.
