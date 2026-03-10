# Modelo de Dados — Usuário

Este documento descreve em profundidade o modelo de **usuário** da Concurseiro API com base no código real do projeto.

O objetivo deste arquivo é documentar:

* a entidade persistida no banco de dados
* os enums e estados do usuário
* os campos públicos e privados
* o fluxo de criação, ativação, autenticação e exclusão
* os contratos de entrada e saída relacionados a usuários

---

# Localização no projeto

Principais classes relacionadas ao modelo de usuário:

```text
src/main/java/br/com/concurseiro/api/usuarios/model/Usuario.java
src/main/java/br/com/concurseiro/api/usuarios/dto/UsuarioPublicoResponse.java
src/main/java/br/com/concurseiro/api/usuarios/service/UsuarioService.java
src/main/java/br/com/concurseiro/api/usuarios/repository/UsuarioRepository.java
src/main/java/br/com/concurseiro/api/usuarios/controller/AuthController.java
src/main/java/br/com/concurseiro/api/usuarios/controller/AdminUsuarioController.java
```

---

# Visão geral do modelo

O usuário é a entidade responsável por representar contas de acesso da aplicação.

A modelagem atual é enxuta e tem foco em autenticação e governança básica de acesso.

Cada usuário possui:

* identificador interno
* nome
* email
* senha armazenada em hash
* papel de acesso
* status de aprovação
* data de criação

A entidade está mapeada para a tabela:

```text
usuarios
```

---

# Entidade persistida

## Classe

```java
Usuario
```

## Tabela

```text
usuarios
```

## Estrutura da entidade

| Campo     | Tipo Java      | Persistido | Obrigatório | Observações                              |
| --------- | -------------- | ---------- | ----------- | ---------------------------------------- |
| id        | Long           | sim        | sim         | chave primária gerada automaticamente    |
| nome      | String         | sim        | sim         | limite de 160 caracteres                 |
| email     | String         | sim        | sim         | único no banco, limite de 200 caracteres |
| senhaHash | String         | sim        | sim         | senha nunca é armazenada em texto puro   |
| role      | Usuario.Role   | sim        | sim         | enum persistido como texto               |
| status    | Usuario.Status | sim        | sim         | enum persistido como texto               |
| criadoEm  | OffsetDateTime | sim        | sim         | inicializado com data/hora atual         |

---

# Campo por campo

## id

Identificador interno do usuário.

Características:

* tipo `Long`
* anotado com `@Id`
* geração automática com `GenerationType.IDENTITY`

Esse campo é usado nas rotas administrativas e na persistência do banco.

---

## nome

Nome do usuário.

Características:

* obrigatório
* persistido com `nullable = false`
* tamanho máximo de `160`

Esse é o nome exibido nas respostas públicas do usuário.

---

## email

Email do usuário.

Características:

* obrigatório
* único
* tamanho máximo de `200`
* utilizado como identificador de login

O sistema consulta usuários por email durante o processo de autenticação.

---

## senhaHash

Armazena o hash da senha.

Características:

* obrigatório
* não deve ser exposto em respostas da API
* é preenchido pelo serviço usando `PasswordEncoder`

A API não salva senha em texto puro. O valor persistido é o hash gerado no cadastro.

---

## role

Representa o papel de acesso do usuário.

Características:

* enum persistido como string
* obrigatório
* controla permissões e autoridade de segurança

Valores possíveis:

| Valor     | Significado           |
| --------- | --------------------- |
| ADMIN     | usuário administrador |
| VISITANTE | usuário visitante     |

A própria entidade fornece o método:

```java
authority()
```

Esse método retorna a autoridade no padrão do Spring Security:

```text
ROLE_ADMIN
ROLE_VISITANTE
```

---

## status

Representa o estado atual do usuário no fluxo de aprovação.

Características:

* enum persistido como string
* obrigatório

Valores possíveis:

| Valor    | Significado                                  |
| -------- | -------------------------------------------- |
| PENDENTE | usuário cadastrado, mas ainda não aprovado   |
| ATIVO    | usuário aprovado e apto a operar normalmente |

Esse campo influencia diretamente a autenticação.

Um usuário com status `PENDENTE` não deve operar como usuário plenamente liberado no sistema.

---

## criadoEm

Data de criação da conta.

Características:

* tipo `OffsetDateTime`
* obrigatório
* inicializado automaticamente com `OffsetDateTime.now()`

Esse campo também é usado para ordenação na listagem administrativa de usuários.

---

# Enums do domínio

## Usuario.Role

Enum responsável pelo papel de acesso.

```java
public enum Role {
    ADMIN,
    VISITANTE
}
```

Além disso, possui o método:

```java
public String authority()
```

Que converte o enum para o padrão esperado pelo Spring Security.

Exemplos:

* `ADMIN` → `ROLE_ADMIN`
* `VISITANTE` → `ROLE_VISITANTE`

---

## Usuario.Status

Enum responsável pelo estado do usuário.

```java
public enum Status {
    PENDENTE,
    ATIVO
}
```

O fluxo atual sugere um modelo simples de aprovação:

1. o usuário se cadastra
2. entra como `VISITANTE`
3. fica com status `PENDENTE`
4. um administrador pode ativá-lo
5. o status passa para `ATIVO`

---

# Regras de negócio do usuário

## Cadastro de visitante

O método de cadastro do serviço é:

```java
cadastrarVisitante(String nome, String email, String senha)
```

Comportamento documentado a partir do código:

1. verifica se já existe usuário com o email informado
2. se existir, retorna erro
3. cria um novo usuário
4. grava o nome e email
5. gera hash da senha com `PasswordEncoder`
6. define `role = VISITANTE`
7. define `status = PENDENTE`
8. salva no banco

### Regra importante

Todo usuário criado pelo fluxo padrão de registro entra como:

```text
role = VISITANTE
status = PENDENTE
```

Ou seja: o sistema não cria usuários comuns já ativos por padrão. Isso é uma decisão arquitetural importante e deve ficar explícita na documentação.

---

## Ativação de usuário

O método administrativo de ativação é:

```java
ativarUsuario(Long id)
```

Comportamento:

1. busca usuário por `id`
2. se não existir, lança erro de não encontrado
3. altera o status para `ATIVO`
4. salva no banco

Esse fluxo mostra que a aplicação possui aprovação administrativa simples baseada em status.

---

## Exclusão de usuário

O método de exclusão administrativa é:

```java
excluirVisitante(Long id)
```

Comportamento:

1. busca usuário por `id`
2. se não existir, lança erro 404
3. verifica o papel do usuário
4. se o usuário for `ADMIN`, a exclusão é bloqueada
5. caso contrário, o registro é removido

### Regra importante

O sistema proíbe explicitamente a exclusão de administradores.

---

## Autenticação de usuário

O serviço também possui o método:

```java
autenticar(String email, String senha)
```

Comportamento:

1. busca usuário por email
2. se não existir, retorna `401 Unauthorized`
3. compara a senha informada com `senhaHash`
4. se a senha não bater, retorna `401 Unauthorized`
5. verifica se o status é `ATIVO`
6. se o status não for `ATIVO`, retorna `403 Forbidden`
7. retorna o usuário autenticado

### Regra crítica

Usuário `PENDENTE` não deve autenticar normalmente como usuário aprovado.

Esse ponto é muito importante para consumidores da API e para a equipe de backend.

---

# Contratos de entrada relacionados ao usuário

Embora `RegisterRequest`, `LoginRequest` e `AuthResponse` estejam declarados como `record` dentro de `AuthController`, eles pertencem diretamente ao domínio de autenticação do usuário e devem ser documentados aqui como modelos relacionados.

## RegisterRequest

Usado em:

```http
POST /api/v1/auth/register
```

### Estrutura

| Campo | Tipo   | Obrigatório | Regras                            |
| ----- | ------ | ----------- | --------------------------------- |
| nome  | String | sim         | `@NotBlank`, máximo 160           |
| email | String | sim         | `@NotBlank`, `@Email`, máximo 200 |
| senha | String | sim         | `@NotBlank`, mínimo 6, máximo 200 |

### Exemplo

```json
{
  "nome": "Maria da Silva",
  "email": "maria@email.com",
  "senha": "123456"
}
```

---

## LoginRequest

Usado em:

```http
POST /api/v1/auth/login
```

### Estrutura

| Campo | Tipo   | Obrigatório | Regras                            |
| ----- | ------ | ----------- | --------------------------------- |
| email | String | sim         | `@NotBlank`, `@Email`, máximo 200 |
| senha | String | sim         | `@NotBlank`, mínimo 6, máximo 200 |

### Exemplo

```json
{
  "email": "usuario@email.com",
  "senha": "123456"
}
```

---

## AuthResponse

Resposta do login.

### Estrutura

| Campo | Tipo   | Descrição                          |
| ----- | ------ | ---------------------------------- |
| token | String | token JWT gerado após autenticação |
| email | String | email do usuário autenticado       |
| role  | String | papel do usuário autenticado       |

### Exemplo

```json
{
  "token": "jwt_token",
  "email": "usuario@email.com",
  "role": "VISITANTE"
}
```

Observação importante: o valor de `role` na resposta vem de `usuario.getRole().name()`.

---

# Contrato público de saída

## UsuarioPublicoResponse

Esse DTO é usado para retornar informações públicas do usuário sem expor `senhaHash`.

### Estrutura

| Campo  | Tipo           | Descrição                |
| ------ | -------------- | ------------------------ |
| id     | Long           | identificador do usuário |
| nome   | String         | nome do usuário          |
| email  | String         | email do usuário         |
| role   | Usuario.Role   | papel do usuário         |
| status | Usuario.Status | estado atual do usuário  |

### Exemplo

```json
{
  "id": 1,
  "nome": "Maria da Silva",
  "email": "maria@email.com",
  "role": "VISITANTE",
  "status": "PENDENTE"
}
```

### Regra de exposição

A senha nunca deve aparecer nas respostas da API.

O DTO público foi criado exatamente para isso: expor apenas os campos seguros e úteis ao cliente.

---

# Repositório

## UsuarioRepository

Responsável pelo acesso ao banco de dados.

Assinatura observada no projeto:

```java
public interface UsuarioRepository extends JpaRepository
```

Métodos documentados:

```java
Optional findByEmail(String email)
boolean existsByEmail(String email)
```

## Papel do repositório

* buscar usuário por email
* verificar unicidade de email
* persistir novas contas
* buscar usuários por id
* listar usuários com paginação

---

# Relação com endpoints

O modelo de usuário é utilizado diretamente nos seguintes documentos de endpoint:

```text
docs/endpoints/auth.md
docs/endpoints/usuarios.md
```

### Fluxos conectados ao modelo

* registro de novo visitante
* login com email e senha
* ativação administrativa
* exclusão administrativa
* listagem administrativa paginada

---

# Regras importantes para integração

Quem consumir a API deve considerar as seguintes regras como obrigatórias:

## 1. Email é único

Não é permitido cadastrar dois usuários com o mesmo email.

## 2. Senha não é retornada

Nenhum contrato público deve expor `senhaHash`.

## 3. Usuário recém-cadastrado não nasce ativo

O cadastro padrão cria:

```text
VISITANTE + PENDENTE
```

## 4. Usuário pendente pode ser barrado no acesso

O fluxo de autenticação do domínio exige status adequado para uso pleno da plataforma.

## 5. Administrador não pode ser excluído por esse fluxo

A regra está implementada explicitamente no serviço.

## 6. Listagem administrativa é ordenada por criação

A paginação administrativa usa ordenação por `criadoEm` em ordem decrescente.

## 7. Tamanho da página é limitado

Na listagem administrativa, o serviço aplica `Math.min(size, 50)`.

Ou seja: mesmo que o cliente envie um valor maior, o backend limita a no máximo 50 registros por página.

---

# Exemplo consolidado do ciclo de vida do usuário

## 1. Registro

```json
{
  "nome": "João Silva",
  "email": "joao@email.com",
  "senha": "123456"
}
```

Resultado esperado no domínio:

```json
{
  "role": "VISITANTE",
  "status": "PENDENTE"
}
```

## 2. Ativação administrativa

Após aprovação administrativa:

```json
{
  "role": "VISITANTE",
  "status": "ATIVO"
}
```

## 3. Login

Após autenticação bem-sucedida:

```json
{
  "token": "jwt_token",
  "email": "joao@email.com",
  "role": "VISITANTE"
}
```

---

# Resumo do modelo

O modelo de usuário da Concurseiro API é simples, seguro e orientado a aprovação administrativa.

Ele separa corretamente:

* dados persistidos
* dados públicos expostos na API
* autenticação baseada em email e senha
* autorização baseada em `role`
* liberação operacional baseada em `status`

Essa distinção entre **papel** e **estado** é uma das partes mais importantes do desenho do domínio de usuários do projeto.
