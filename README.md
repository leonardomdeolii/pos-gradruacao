# Cadastro de Usuários

Aplicação Spring Boot que expõe um CRUD REST de usuários, com persistência em H2 via JDBC puro e uma interface web estática para consumir a API.

## Stack

- Java 21
- Spring Boot 3.3.4 (`spring-boot-starter-web`)
- H2 Database 2.2.224 (modo arquivo, `AUTO_SERVER=TRUE`)
- Gradle Wrapper
- JUnit 5

## Pré-requisitos

- JDK 21 instalado (o toolchain do Gradle tenta baixar se não encontrar).
- Não é necessário ter Gradle instalado — use o wrapper (`./gradlew`).

## Como executar

```bash
./gradlew bootRun
```

A aplicação sobe em `http://localhost:8080`. A interface web fica disponível em `http://localhost:8080/index.html`.

Outros comandos úteis:

```bash
./gradlew build     # compila e empacota o jar
./gradlew test      # executa os testes
```

## Como executar com Docker

Pré-requisito: Docker Desktop (ou daemon compatível) em execução.

1. **Construir a imagem** a partir da raiz do projeto:

   ```bash
   docker build -t pos-graduacao:latest .
   ```

2. **Subir o container** (porta 8080 exposta, banco efêmero dentro do container):

   ```bash
   docker run -d --name pos-graduacao -p 8080:8080 pos-graduacao:latest
   ```

   Para persistir o H2 entre execuções, monte o diretório `data/` como volume:

   ```bash
   docker run -d --name pos-graduacao \
     -p 8080:8080 \
     -v "$(pwd)/data:/app/data" \
     pos-graduacao:latest
   ```

3. **Validar que subiu**:

   ```bash
   curl http://localhost:8080/usuarios        # deve responder 200
   open http://localhost:8080/index.html      # UI web
   ```

4. **Comandos de operação**:

   ```bash
   docker logs -f pos-graduacao   # acompanhar logs
   docker stop pos-graduacao      # parar
   docker start pos-graduacao     # iniciar novamente
   docker rm -f pos-graduacao     # remover container
   ```

Opções de ajuste via variável de ambiente `JAVA_OPTS` (ex.: `-e JAVA_OPTS="-Xmx256m"`).

## Banco de dados

O H2 grava em arquivo no diretório `./data/` (`posgraduacao.mv.db`), então os dados persistem entre execuções. Para resetar o estado basta apagar o diretório `data/` — a tabela `usuario` é recriada automaticamente na próxima inicialização.

Configuração da conexão (hardcoded em `DatabaseConnection`):

- URL: `jdbc:h2:./data/posgraduacao;AUTO_SERVER=TRUE`
- Usuário: `sa`
- Senha: *(vazia)*

Graças ao `AUTO_SERVER=TRUE`, é possível conectar com um cliente externo (ex.: H2 Console, DBeaver) enquanto a aplicação está em execução.

## Endpoints

Base path: `/usuarios`

| Método | Caminho           | Descrição                      | Status de sucesso |
|--------|-------------------|--------------------------------|-------------------|
| POST   | `/usuarios`       | Cria um novo usuário           | 201 Created       |
| GET    | `/usuarios`       | Lista todos os usuários        | 200 OK            |
| GET    | `/usuarios/{id}`  | Busca um usuário pelo id       | 200 OK / 404      |
| PUT    | `/usuarios/{id}`  | Atualiza nome e e-mail         | 200 OK / 404      |
| DELETE | `/usuarios/{id}`  | Remove o usuário               | 204 No Content    |

Corpo das requisições de `POST` e `PUT`:

```json
{
  "nome": "Fulano da Silva",
  "email": "fulano@example.com"
}
```

Respostas de erro:

- `400 Bad Request` — `nome` ou `email` ausentes/em branco.
- `404 Not Found` — id inexistente em `GET/PUT/DELETE`.
- `409 Conflict` — e-mail já cadastrado (violação da constraint `UNIQUE`).

### Exemplos com `curl`

```bash
# Criar
curl -X POST http://localhost:8080/usuarios \
  -H 'Content-Type: application/json' \
  -d '{"nome":"Fulano","email":"fulano@example.com"}'

# Listar
curl http://localhost:8080/usuarios

# Buscar por id
curl http://localhost:8080/usuarios/1

# Atualizar
curl -X PUT http://localhost:8080/usuarios/1 \
  -H 'Content-Type: application/json' \
  -d '{"nome":"Fulano Editado","email":"novo@example.com"}'

# Remover
curl -X DELETE http://localhost:8080/usuarios/1
```

## Estrutura do projeto

```
src/main/java/org/example/
├── DemoApplication.java          # bootstrap Spring Boot
├── config/DatabaseConnection.java # factory de Connection JDBC
├── controller/UsuarioController.java
├── exception/GlobalExceptionHandler.java
├── model/Usuario.java
└── repository/UsuarioDAO.java    # SQL + PreparedStatement

src/main/resources/
├── application.properties
└── static/index.html             # UI web que consome a API
```

## Notas de arquitetura

- O projeto usa **JDBC puro** de propósito — não há Spring Data / JPA. O `DataSourceAutoConfiguration` está excluído em `application.properties`, e o `UsuarioDAO` gerencia conexões manualmente (sem pool).
- Não há camada de serviço: o controller chama o DAO diretamente. Exceções de SQL são encapsuladas como `RuntimeException` no DAO e traduzidas para HTTP pelo controller / `GlobalExceptionHandler`.
- A tabela `usuario` é criada de forma idempotente (`CREATE TABLE IF NOT EXISTS`) tanto pelo `@PostConstruct` do DAO quanto pelo `CommandLineRunner` em `DemoApplication`.
