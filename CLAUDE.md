# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

Build / run / test use the Gradle wrapper (Java 21 toolchain, Spring Boot 3.3.4):

- Run the app: `./gradlew bootRun` — serves on `http://localhost:8080`, static UI at `/index.html`.
- Build jar: `./gradlew build`
- Run tests: `./gradlew test`
- Run a single test: `./gradlew test --tests 'org.example.SomeClassTest.someMethod'`

The H2 database file lives at `./data/posgraduacao.mv.db` and persists between runs. Deleting the `data/` directory resets state; the schema is re-created on startup.

## Architecture

Spring Boot REST app for user CRUD (`Usuario`), deliberately using **raw JDBC instead of Spring Data / JPA**. Key consequences:

- `application.properties` excludes `DataSourceAutoConfiguration` — do not add `spring.datasource.*` properties expecting them to take effect. Connections come from `org.example.config.DatabaseConnection.get()`, which hardcodes the H2 JDBC URL (`jdbc:h2:./data/posgraduacao;AUTO_SERVER=TRUE`, user `sa`, no password).
- `UsuarioDAO` (`@Repository`) owns all SQL. It opens a fresh `Connection` per method via try-with-resources — there is no connection pool. The `usuario` table is created idempotently both from `@PostConstruct` and from the `CommandLineRunner` in `DemoApplication`.
- `UsuarioController` (`/usuarios`) calls the DAO directly; there is no service layer. It translates DAO `RuntimeException` on insert into HTTP 409 (assumed UNIQUE-email violation) and missing rows into 404 via `ResponseStatusException`. Request body is the inner `UsuarioRequest` record; responses serialize the `Usuario` POJO (plain getters, no Jackson annotations).
- `GlobalExceptionHandler` is the cross-cutting error mapper — prefer extending it over adding try/catch in controllers.
- Static frontend (`src/main/resources/static/index.html`) is a single HTML file that calls the REST endpoints; it ships with the jar.

When adding persistence features, keep the pattern: SQL + `PreparedStatement` in the DAO, wrap `SQLException` as `RuntimeException`, and surface HTTP semantics in the controller.
