package org.example.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Centraliza a obtenção de conexões com o banco H2.
 *
 * Usa o modo "file" para que os dados persistam entre execuções.
 * O arquivo do banco será criado em ./data/posgraduacao.mv.db
 */
public class DatabaseConnection {

    private static final String URL = "jdbc:h2:./data/posgraduacao;AUTO_SERVER=TRUE";
    private static final String USER = "sa";
    private static final String PASSWORD = "";

    private DatabaseConnection() {
    }

    public static Connection get() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}

