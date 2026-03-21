# Concurseiro API

API REST para gerenciamento de **questões de concursos públicos**, provas, comentários e usuários.

O projeto foi desenvolvido com **Spring Boot** e possui arquitetura modular, autenticação baseada em **JWT**, organização por catálogos estruturados, observabilidade e controle de acesso por roles.

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
* monitoramento com Actuator e Prometheus
* limitação de tentativas no endpoint de login

A API foi projetada para servir aplicações web ou mobile voltadas para **estudo de concursos públicos**.

---

# Stack tecnológica

Principais tecnologias utilizadas:

* Java 21
* Spring Boot
* Spring Web
* Spring Security
* JWT (JJWT)
* Spring Data JPA
* PostgreSQL
* Liquibase
* Springdoc OpenAPI (Swagger)
* Spring Boot Actuator
* Micrometer Prometheus
* Redis

---

# Arquitetura

A aplicação segue uma arquitetura em camadas:

Controller → Service → Repository → Banco de Dados

Cada camada possui responsabilidades bem definidas:

**Controller**

* recebe requisições HTTP
* valida entradas
* chama os serviços da aplicação
* retorna respostas

**Service**

* implementa regras de negócio
* coordena operações
* aplica validações de domínio

**Repository**

* acesso ao banco de dados
* persistência e consulta de entidades

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

```text
docs/
```

## Documentos principais

Arquitetura do sistema:

```text
docs/arquitetura.md
```

Autenticação e segurança:

```text
docs/autenticacao.md
```

Paginação e filtros:

```text
docs/paginacao.md
```

Tratamento de erros:

```text
docs/erros.md
```

---

# Documentação de endpoints

Endpoints da API estão organizados em:

```text
docs/endpoints/
```

Principais módulos documentados:

```text
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

```text
docs/modelos/
```

Modelos disponíveis:

```text
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

```http
Authorization: Bearer <token>
```

A API utiliza autorização baseada em roles.

**VISITANTE**
- pode realizar ações autenticadas comuns, como criar provas, cadastrar questões, comentar questões e interagir com comentários

**ADMIN**
- possui acesso administrativo
- pode aprovar usuários
- pode excluir usuários visitantes
- pode excluir questões
- pode acessar endpoints administrativos
- pode acessar endpoints protegidos de observabilidade

Alguns endpoints de leitura da API permanecem públicos, como busca de questões, listagem de provas e leitura de catálogo.

---

# Controle de acesso

De acordo com a configuração atual de segurança:

Rotas públicas:
- `POST /api/v1/auth/**`
- `GET /api/v1/questoes/**`
- `GET /api/v1/catalogo/**`
- `GET /api/v1/provas/**`
- `GET /api/v1/questoes/*/comentarios`
- `/actuator/health`
- `/actuator/info`
- `/v3/api-docs/**`
- `/swagger-ui/**`
- `/swagger-ui.html`

Rotas autenticadas para `VISITANTE` e `ADMIN`:
- `POST /api/v1/questoes`
- `POST /api/v1/questoes/*/comentarios`
- `POST /api/v1/provas`
- `POST /api/v1/provas/*/questoes`
- `POST /api/v1/comentarios/*/curtir`
- `POST /api/v1/comentarios/*/descurtir`

Rotas exclusivas de `ADMIN`:
- `/api/v1/admin/**`
- `/actuator/prometheus`
- `/internal/prometheus`

---

# Proteção contra abuso no login

O endpoint `POST /api/v1/auth/login` possui limitação de tentativas para reduzir abuso e força bruta.

A limitação considera:
- IP do cliente
- email enviado no corpo da requisição

A implementação atual utiliza Redis.

---

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

```text
openapi.json
```

Quando a aplicação está rodando em ambiente com Swagger habilitado, a interface pode ser acessada pelos endpoints configurados pelo Springdoc.

Observação:
- em produção, o Swagger está desabilitado por configuração

---

# Banco de dados e persistência

A aplicação utiliza:

* PostgreSQL como banco principal
* Liquibase para gerenciamento de changelog
* Hibernate com `ddl-auto=validate`

Isso significa que o schema não deve ser alterado automaticamente pela aplicação.

---

# Configuração

Principais variáveis e propriedades utilizadas:

## Banco
- `PGHOST`
- `PGPORT`
- `PGDATABASE`
- `PGUSER`
- `PGPASSWORD`

## JWT
- `JWT_SECRET` obrigatória
- `JWT_EXPIRATION_MS` opcional

## CORS
- `CORS_ALLOWED_ORIGINS`

## Aplicação
- `spring.application.name=concurseiro-api`

---

# Executando o projeto

Pré-requisitos:

* Java 21
* Maven
* PostgreSQL
* Redis
* variável `JWT_SECRET` configurada

Clone o repositório:

```bash
git clone https://github.com/carmaunt/concurseiro-api
```

Entre no diretório:

```bash
cd concurseiro-api
```

Execute o projeto:

```bash
./mvnw spring-boot:run
```

A API será iniciada na porta padrão:

```text
http://localhost:8080
```

---

# Monitoramento

O projeto utiliza **Spring Boot Actuator** e **Prometheus** para saúde e métricas da aplicação.

Endpoints expostos na configuração atual:

```text
/actuator/health
/actuator/info
/actuator/prometheus
```

Observações:
- `/actuator/health` e `/actuator/info` são públicos
- `/actuator/prometheus` exige role `ADMIN`
- em produção, os endpoints expostos são mais restritos

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
