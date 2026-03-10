# Arquitetura — Concurseiro API

Este documento descreve a arquitetura técnica da **Concurseiro API**, explicando como o sistema está organizado internamente, quais são os principais módulos e como os componentes se comunicam.

---

# Visão geral

A Concurseiro API é uma **API REST construída com Spring Boot** cujo objetivo é gerenciar questões de concursos públicos, provas, usuários e comentários.

A aplicação segue uma arquitetura baseada em **camadas**, comum em aplicações Spring:

Controller → Service → Repository → Banco de dados

Cada camada possui uma responsabilidade bem definida, reduzindo acoplamento e facilitando manutenção e evolução do sistema.

---

# Stack tecnológica

Principais tecnologias utilizadas no projeto:

* Java
* Spring Boot
* Spring Web
* Spring Security
* JWT (JSON Web Token)
* Spring Data JPA
* PostgreSQL
* Springdoc OpenAPI
* Actuator
* Micrometer / Prometheus

Essas tecnologias permitem criar uma API robusta, observável e segura.

---

# Arquitetura em camadas

## Controller

Responsável por expor os endpoints HTTP da aplicação.

Funções da camada:

* receber requisições HTTP
* validar dados de entrada
* chamar os serviços da aplicação
* retornar respostas HTTP

Exemplos de controllers no projeto:

* AuthController
* QuestaoController
* ProvaController
* ComentarioController
* AdminUsuarioController

---

## Service

Responsável pelas **regras de negócio** da aplicação.

Funções da camada:

* implementar lógica do domínio
* validar regras do sistema
* orquestrar acesso a dados
* chamar repositórios

Controllers nunca devem acessar o banco diretamente. Toda lógica passa pelos serviços.

---

## Repository

Responsável pelo acesso ao banco de dados.

Utiliza **Spring Data JPA** para persistência.

Funções da camada:

* salvar entidades
* buscar registros
* executar queries

Repositories normalmente estendem:

```
JpaRepository
```

Isso fornece automaticamente operações como:

* save
* findById
* findAll
* delete

---

## Entidades (Model)

Representam as estruturas persistidas no banco de dados.

Exemplos de entidades do sistema:

* Usuario
* Questao
* Prova
* Comentario
* Banca
* Disciplina
* Assunto

Essas classes utilizam anotações JPA como:

* @Entity
* @Id
* @ManyToOne
* @OneToMany

---

# Organização por módulos

O projeto está organizado por **contextos de domínio**.

Principais módulos:

## usuarios

Responsável por:

* cadastro de usuários
* autenticação
* administração de contas

Controllers relacionados:

* AuthController
* AdminUsuarioController

---

## questoes

Responsável por gerenciar questões de concursos.

Funções do módulo:

* cadastro de questões
* busca e filtros
* edição e remoção

Esse é o **módulo central do sistema**.

---

## provas

Gerencia provas e sua relação com questões.

Funções:

* cadastro de provas
* associação de questões

---

## comentarios

Permite registrar comentários em entidades do sistema.

Funções:

* criar comentários
* listar comentários

---

## catalogo

Responsável por dados auxiliares usados para classificação.

Exemplos:

* banca
* disciplina
* assunto
* subassunto
* instituição

---

## admin

Contém endpoints administrativos utilizados para gerenciamento avançado do sistema.

---

# Segurança da aplicação

A segurança é baseada em **JWT (JSON Web Token)**.

Fluxo de autenticação:

1. usuário envia email e senha
2. API valida as credenciais
3. API gera um token JWT
4. cliente utiliza o token nas próximas requisições

Header utilizado:

```
Authorization: Bearer <token>
```

Spring Security intercepta as requisições e valida o token antes de permitir acesso aos endpoints protegidos.

---

# Observabilidade

A aplicação inclui ferramentas de monitoramento:

## Actuator

Fornece endpoints de saúde e métricas da aplicação.

Exemplos:

* /actuator/health
* /actuator/info
* /actuator/metrics

## Prometheus

Permite coleta de métricas da aplicação.

Essas métricas podem ser utilizadas para monitoramento em produção.

---

# Documentação da API

A API utiliza **OpenAPI / Swagger** para documentação automática.

Arquivos relacionados:

* openapi.json

A interface Swagger permite explorar e testar os endpoints da API.

---

# Fluxo de requisição

Exemplo de fluxo típico dentro da aplicação:

1. Cliente envia requisição HTTP
2. Controller recebe a requisição
3. Controller chama o Service
4. Service executa regras de negócio
5. Service chama o Repository
6. Repository acessa o banco
7. Resposta retorna ao cliente

---

# Benefícios da arquitetura adotada

* separação clara de responsabilidades
* facilidade de manutenção
* escalabilidade
* facilidade de testes
* organização por domínio

Essa arquitetura segue padrões comuns em aplicações Spring Boot modernas.
