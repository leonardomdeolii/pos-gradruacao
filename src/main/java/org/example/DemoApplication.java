package org.example;
import org.example.repository.UsuarioDAO;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
/**
 * Aplicação Spring Boot para cadastro de usuários expondo endpoints REST.
 */
@SpringBootApplication
public class DemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
    /** Garante a criação da tabela ao subir a aplicação. */
    @Bean
    public CommandLineRunner init(UsuarioDAO dao) {
        return args -> dao.criarTabela();
    }
}
