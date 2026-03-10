# Concurseiro API

API REST para gerenciamento de **questões de concursos públicos**, provas, comentários e usuários.

O projeto foi desenvolvido com **Spring Boot** e possui arquitetura modular, autenticação baseada em **JWT**, busca avançada de questões e organização por catálogos estruturados.

---

# Visão geral

A Concurseiro API permite:

* cadastro de usuários visitantes com aprovação administrativa
* autenticação baseada em JWT para usuários ativos
* gerenciamento de questões de concursos
* organização de provas
* comentários em questões
* classificação por disciplina, banca, assunto e instituição
* busca avançada com filtros e paginação

A API foi projetada para servir aplicações web ou mobile voltadas para **estudo de concursos públicos**.

---

# Stack tecnológica

Principais tecnologias utilizadas:

* Java
* Spring Boot
* Spring Web
* Spring Security
* JWT (JSON Web Token)
* Spring Data JPA
* PostgreSQL
* Springdoc OpenAPI (Swagger)
* Actuator

---

# Arquitetura

A aplicação segue uma arquitetura em camadas:

Controller → Service → Repository → Banco de Dados

Cada camada possui responsabilidades bem definidas:

**Controller**

* recebe requisições HTTP
* valida entradas
* retorna respostas

**Service**

* implementa regras de negócio
* coordena operações

**Repository**

* acesso ao banco de dados

---

# Estrutura do projeto

```text
src/main/java/br/com/concurseiro/api

admin
catalogo
comentario
infra
prova
questoes
usuarios
```

Cada módulo representa um contexto funcional da aplicação.

---

# Documentação completa

Toda a documentação da API está disponível na pasta:

```
docs/
```

## Documentos principais

Arquitetura do sistema:

```
docs/arquitetura.md
```

Autenticação e segurança:

```
docs/autenticacao.md
```

Paginação e filtros:

```
docs/paginacao.md
```

Tratamento de erros:

```
docs/erros.md
```

---

# Documentação de endpoints

Endpoints da API estão organizados em:

```
docs/endpoints/
```

Principais módulos documentados:

```
auth.md
usuarios.md
questoes.md
provas.md
comentarios.md
catalogo.md
```

---

# Modelos de dados

A estrutura das entidades da API está documentada em:

```
docs/modelos/
```

Modelos disponíveis:

```
usuario.md
questao.md
prova.md
comentario.md
```

---

# Autenticação

A API utiliza autenticação baseada em **JWT**.

Fluxo de autenticação:

1. usuário realiza cadastro
2. o usuário é criado com status **PENDENTE**
3. um administrador aprova o usuário
4. o usuário realiza login
5. a API gera um token JWT
6. o cliente envia o token nas requisições autenticadas

Header utilizado:

```
Authorization: Bearer <token>
```

---

A API utiliza autorização baseada em roles.

VISITANTE  
- pode realizar ações autenticadas comuns como criar provas e comentar questões

ADMIN  
- possui acesso administrativo
- pode aprovar usuários
- pode excluir usuários visitantes
- pode excluir questões

Alguns endpoints de leitura da API permanecem públicos, como busca de questões e listagem de provas.

# Exemplo de uso

## Criar usuário

```http
POST /api/v1/auth/register
```

```json
{
  "nome": "Maria da Silva",
  "email": "maria@email.com",
  "senha": "123456"
}
```

---

## Login

```http
POST /api/v1/auth/login
```

```json
{
  "email": "maria@email.com",
  "senha": "123456"
}
```

Resposta:

```json
{
  "success": true,
  "data": {
    "token": "jwt_token",
    "email": "maria@email.com",
    "role": "VISITANTE"
  }
}
```

Observação:

Usuários com status **PENDENTE** não podem realizar login e receberão resposta HTTP 403.
---

## Buscar questões

```http
GET /api/v1/questoes?page=0&size=10
```

---

# Swagger / OpenAPI

A API possui especificação **OpenAPI**.

Arquivo:

```
openapi.json
```

Quando a aplicação está rodando, a interface Swagger pode ser acessada normalmente pelo endpoint configurado pelo Springdoc.

---

# Executando o projeto

Pré-requisitos:

* Java 17+
* Maven
* PostgreSQL

Clone o repositório:

```
git clone https://github.com/carmaunt/concurseiro-api
```

Entre no diretório:

```
cd concurseiro-api
```

Execute o projeto:

```
./mvnw spring-boot:run
```

A API será iniciada na porta padrão:

```
http://localhost:8080
```

---

# Monitoramento

O projeto utiliza **Spring Actuator** para métricas e saúde da aplicação.

Endpoints comuns:

```
/actuator/health
/actuator/info
/actuator/metrics
```

---

# Licença

Este projeto é disponibilizado para fins educacionais e de estudo sobre desenvolvimento de APIs para plataformas de concursos públicos.

---

# Autor

Projeto desenvolvido por **Carmaunt**.

## Modelo de acesso

A API possui dois tipos de usuários:

VISITANTE  
ADMIN

Todo usuário registrado inicia com status **PENDENTE**.

Usuários pendentes não podem se autenticar na API.

Um administrador deve aprovar o usuário, alterando seu status para **ATIVO**.

Somente usuários **ATIVOS** podem utilizar endpoints autenticados da API.
